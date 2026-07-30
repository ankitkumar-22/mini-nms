# mini-nms — Engineering & Interview Study Guide

A reference for explaining this project in interviews: what it does, the tech stack and *why* each piece is there, and every concept it demonstrates — with how each one shows up in the code and the questions you should be ready to field.

> **Honesty marker used throughout:** ✅ = built and you can speak to it in detail. 🔜 = designed / planned; discuss as "the next thing I'd add and here's how." Don't claim 🔜 items as done.

---

## 1. The pitch

**Resume line:** *"Re-architected a single-node network monitor into a horizontally-scalable, fault-tolerant distributed system using Kafka, consumer groups, and a MongoDB-backed distributed lock."*

**60-second verbal walkthrough:**
> "It's a network monitoring service — you register devices by IP, and it pings them on a schedule and records up/down status, latency, and packet loss, exposed over a REST API. It started as a monolith: one node, a timer that pinged every device serially. That has three problems — single point of failure, can't scale because pinging is blocking I/O, and you can't run two copies without double-pinging. So I split it into four roles that ship in one JAR and are chosen by Spring profile: a *scheduler* that publishes one ping job per device to Kafka, a *worker* pool that consumes those jobs as a consumer group and does the actual pinging, and an *ingestion* consumer that writes results back to MongoDB. The scheduler is the only thing that must be single-active, so I guard it with ShedLock, a Mongo-backed distributed lock. Messages are keyed by device ID so each device's events stay ordered, and the ingestion writes are idempotent because Kafka is at-least-once."

---

## 2. The core problem → the fix

| Monolith problem | Root cause | How the distributed design fixes it |
|------------------|-----------|--------------------------------------|
| Single point of failure | One node does everything | Independent roles; Kafka reassigns a dead worker's partitions automatically |
| Can't scale | Serial, blocking pings can't finish the 60s window at scale | Workers are a Kafka consumer group — add instances to add throughput |
| Can't run multiple copies | Two schedulers double-ping and fight over the same document | ShedLock makes the scheduler single-active; results keyed by device keep writes ordered and idempotent |

---

## 3. Tech stack

| Technology | What it is | Role in this project | Why it was chosen |
|-----------|------------|----------------------|-------------------|
| **Java 21** | Language/runtime | All services | Modern LTS; unlocks virtual threads (🔜) for blocking ping fan-out |
| **Spring Boot 4** | Application framework | DI, config, web, scheduling, autoconfig | Fast, conventional, huge ecosystem |
| **Spring Web MVC** | REST framework | The `web` role's API | Standard servlet-based REST |
| **Spring Data MongoDB** | Data-access abstraction | Repositories + `MongoTemplate` updates | Removes boilerplate; derived query methods |
| **MongoDB** | Document (NoSQL) database | Stores devices + metric time-series | Flexible schema; easy append-only metrics; TTL/sharding options |
| **Apache Kafka** | Distributed event log / message bus | Decouples scheduler → worker → ingestion | Partitioned ordering, consumer groups, replayable log, high throughput |
| **Spring Kafka** | Spring integration for Kafka | `@KafkaListener`, `KafkaTemplate`, JSON (de)serialization | Idiomatic producers/consumers |
| **ShedLock** | Distributed lock for scheduled tasks | Makes the scheduler single-active | Pragmatic leader-election alternative; reuses existing MongoDB |
| **Bean Validation** | Declarative input validation | Validates device requests (IPv4, non-blank) | Clean, annotation-driven |
| **Spring Boot Actuator** | Ops endpoints | `/actuator/health` | Health checks for orchestration |
| **Lombok** | Boilerplate reduction | Getters/setters on models | Less noise |
| **Docker / Docker Compose** | Containerization | Runs MongoDB and Kafka locally | Reproducible local infra |
| **Maven** | Build tool | Build + dependency management | Standard for Spring |

---

## 4. Concepts covered

### 4.1 Distributed systems & messaging

**Event-driven architecture / decoupling** ✅
Components communicate via asynchronous messages instead of direct calls. *Here:* the scheduler doesn't call the worker — it publishes to a topic; producers and consumers don't know about each other. *Be ready to explain:* benefits (independent scaling, fault isolation, buffering/backpressure) and costs (eventual consistency, harder debugging, need for idempotency).

**Kafka topics, partitions, and partition keys** ✅
A topic is a named log split into partitions; the message *key* is hashed to choose a partition. *Here:* `ping-jobs` and `ping-results` have 3 partitions each, keyed by `deviceId`. *Be ready to explain:* a partition is the unit of parallelism and ordering; choosing a good key matters; partition count caps consumer parallelism.

**Consumer groups & rebalancing** ✅
Consumers sharing a `group.id` split a topic's partitions among themselves — at most one consumer per partition is active. Adding/removing consumers triggers a rebalance. *Here:* `ping-worker-group` and `ingestion-group`; starting a 3rd worker rebalances the 3 partitions across 3 instances. *Be ready to explain:* this is how work is distributed without you writing distribution logic; max useful workers = partition count.

**Delivery semantics & idempotency** ✅
Kafka is **at-least-once** by default — a message can be redelivered (e.g., a consumer crashes after processing but before committing its offset). *Here:* ingestion uses an idempotent targeted update (`$set status/lastChecked` by id) so applying the same result twice is harmless. *Be ready to explain:* at-most-once vs at-least-once vs exactly-once, and why idempotency is the practical answer.

**Ordering guarantees** ✅
Kafka guarantees order only *within a partition*. *Here:* keying by `deviceId` puts all of one device's events on the same partition, so its status transitions apply in order even with many workers. *Be ready to explain:* there is no global ordering across partitions, and that's a deliberate trade for scalability.

**Producer / consumer pattern & backpressure** ✅
Work is produced to a buffer and consumed at the consumers' pace. *Here:* consumer lag (visible via `kafka-consumer-groups.sh`) tells you if workers are keeping up. *Be ready to explain:* lag as a backpressure signal; how you'd scale on it.

**Replication factor** ✅ (concept) / 🔜 (multi-broker)
Partitions can be replicated across brokers for durability. *Here:* RF=1 on the single-broker dev setup; you'd raise it on a real cluster. *Be ready to explain:* leader/follower replicas, ISR, the durability-vs-cost trade.

**Dead-letter topics / poison messages** 🔜
A message that can't be processed shouldn't loop forever; route it to a dead-letter topic. *Here:* not added yet (single JAR means matching classes, so deserialization won't fail in practice). *Be ready to explain:* `ErrorHandlingDeserializer` + DLT as the fix.

### 4.2 Coordination & concurrency

**Distributed locking / leader election** ✅
Ensuring only one node performs an action across a cluster. *Here:* ShedLock acquires a lease in MongoDB (`shedLock` collection) so only one scheduler dispatches per cycle. *Be ready to explain:* ShedLock is a **lease-based lock, not consensus** — if `lockAtMostFor` is shorter than actual runtime the lock expires and a second node can run. The "real" alternative is ZooKeeper/Raft-style leader election; you chose ShedLock as the pragmatic option that reuses Mongo. Mention **split-brain** and clock-skew (`lockAtLeastFor`).

**Optimistic concurrency control** ✅
Detect concurrent modifications with a version field instead of locking. *Here:* `Device` has `@Version`; a stale write throws `OptimisticLockingFailureException`. *Be ready to explain:* optimistic vs pessimistic locking, the **lost-update problem**, and why you moved the status write to an idempotent targeted update (read-modify-save fights the version under concurrency; a `$set` heartbeat doesn't).

**Concurrency, blocking I/O, and virtual threads** 🔜
A ping is blocking I/O (up to 4 packets × 5s). *Planned:* have each worker ping many devices concurrently using Java 21 virtual threads. *Be ready to explain:* platform vs virtual threads, why virtual threads suit blocking I/O fan-out (cheap to block, JVM-scheduled), and the alternative of a bounded thread pool.

### 4.3 Fault tolerance & reliability

**Single point of failure / failover** ✅
*Here:* any role can be run multiply; killing a worker triggers a rebalance and the rest carry on; killing the active scheduler hands the lock to another within a cycle.

**Caching (cache-aside)** 🔜
*Planned:* Redis caching of current status and latest metrics, read-through on miss. *Be ready to explain:* cache-aside pattern, TTL, and cache invalidation as a known hard problem.

**Circuit breaker / retry / bulkhead / timeout** 🔜
*Planned:* Resilience4j around pings and datastore calls. *Be ready to explain:* what each pattern prevents (cascading failure, thundering retries, resource exhaustion).

### 4.4 Spring & backend

**Dependency injection & Spring Boot autoconfiguration** ✅
Constructor injection throughout; Boot wires beans from the classpath and properties.

**Profiles for role-based deployment** ✅
One artifact, multiple roles selected by `@Profile` + `--spring.profiles.active`. *Be ready to explain:* why this beats four separate repos for a small system (one build, shared code), and when you'd split into true microservices.

**REST API design + DTO separation** ✅
*Here:* `DeviceRequestDTO` (only `name`/`ipAddress`) vs `DeviceResponseDTO` (adds `id`, `status`, timestamps) so clients can't set server-owned fields. *Be ready to explain:* why you don't expose entities directly.

**Bean validation** ✅
*Here:* `@NotBlank`, IPv4 `@Pattern`; failures become structured 400s.

**Global exception handling** ✅
*Here:* `@RestControllerAdvice` maps exceptions to HTTP statuses (404/409/400/500) with a consistent JSON body, and logs internals server-side without leaking them.

**Scheduling** ✅
*Here:* `@Scheduled(fixedRate=60000)` drives the dispatch cycle, guarded by `@SchedulerLock`.

### 4.5 Data & persistence

**Document data modeling** ✅
*Here:* `devices` and `network_metrics` collections; response/request split; unique IP.

**Time-series growth & retention** 🔜
Metrics grow unbounded. *Planned:* a MongoDB TTL index to expire old metrics, with sharding/rollups as the scale story. *Be ready to explain:* hot/cold data, partitioning a time-series workload.

**Targeted updates vs full saves** ✅
*Here:* ingestion uses `MongoTemplate.updateFirst(...$set...)` instead of `save()` — cheaper, atomic, idempotent, and avoids the version-classification insert trap.

### 4.6 Observability 🔜

**Metrics, tracing** — *Planned:* Micrometer → Prometheus → Grafana for JVM/ping/queue-lag metrics; OpenTelemetry tracing that propagates context **across the Kafka boundary** (the hard, impressive part — correlating one device's ping scheduler → worker → ingestion). *Be ready to explain:* why async tracing is non-trivial (no thread-local continuity; context rides in message headers).

### 4.7 Packaging & ops

**Containerization & Docker Compose** ✅ (infra) / 🔜 (full orchestration)
*Here:* Mongo + Kafka run in Docker; the app runs from the JAR with profiles. *Planned:* one compose/k8s setup running all roles + infra.

**Kafka KRaft mode** ✅
The dev broker runs without ZooKeeper (KRaft = Kafka's built-in Raft metadata quorum). *Be ready to explain:* what ZooKeeper used to do and why KRaft replaced it.

**Container networking & capabilities** ✅ (aware)
Pinging from a container needs the `ping` binary and the `NET_RAW` capability (ICMP). *Be ready to explain:* why containers drop capabilities by default.

---

## 5. Design decisions to defend

| Decision | Why | The alternative & when you'd pick it |
|----------|-----|--------------------------------------|
| Kafka over RabbitMQ | Partitioned ordering, replayable log, consumer-group scaling, high throughput | RabbitMQ for complex routing, per-message ack, lower-latency small workloads |
| ShedLock over ZooKeeper | Pragmatic, zero extra infra (reuses Mongo) | ZooKeeper/Raft for true consensus / strict leader election |
| MongoDB | Flexible schema, easy append-only metrics | A real time-series DB (e.g. Timescale/Influx) at higher scale |
| One JAR + profiles | One build, shared code, easy to run/demo | Separate microservices when teams/scaling/deploy cadences diverge |
| Idempotent `$set` ingestion | Safe under at-least-once redelivery; no version contention | Versioned `save()` only where true read-modify-write conflict matters |
| Key by `deviceId` | Per-device ordering and even distribution | Key by region/tenant if that's the ordering boundary |

---

## 6. Implemented vs planned

**Implemented (speak in detail):** Kafka decomposition (jobs/results topics, keyed messages), worker + ingestion consumer groups, profile-based roles in one JAR, ShedLock single-active scheduler, MongoDB persistence, REST API with DTOs/validation/global error handling, optimistic locking + idempotent targeted updates, Dockerized Mongo + KRaft Kafka.

**Planned (discuss as next steps):** virtual-thread concurrency in workers, Redis cache-aside, Resilience4j, MongoDB TTL retention, dead-letter handling, Micrometer/Prometheus/Grafana, OpenTelemetry tracing across Kafka, real-time dashboard (WebSocket/SSE), full Compose/Kubernetes orchestration.

---

## 7. Rapid-fire questions to rehearse

- Why is the scheduler the only single-active component, and how do you enforce it?
- What happens, step by step, when a worker dies mid-cycle?
- Why key by `deviceId`? What breaks if you don't?
- How many workers can usefully run, and what limits it?
- How do you handle a result delivered twice?
- ShedLock vs ZooKeeper — when does ShedLock's guarantee fail?
- Why did the duplicate-key error happen on the device write, and how does optimistic locking decide insert vs update?
- Why virtual threads for pinging instead of a thread pool?
- How would you add exactly-once, and is it worth it here?
- How would you trace one device's ping across the async boundaries?

---

## 8. Glossary

- **Partition** — an ordered, independently-consumed shard of a Kafka topic; the unit of parallelism and ordering.
- **Consumer group** — consumers sharing a `group.id` that split a topic's partitions; the basis of work distribution.
- **Offset** — a consumer's position in a partition; committing it marks messages as processed.
- **Rebalance** — reassignment of partitions when group membership changes.
- **At-least-once** — every message is delivered, possibly more than once; demands idempotent processing.
- **Idempotency** — applying an operation multiple times yields the same result as once.
- **Optimistic locking** — concurrency control via a version field; conflicts are detected, not prevented.
- **Lease-based lock** — a lock held for a bounded time that auto-expires (ShedLock), as opposed to consensus-based locking.
- **Split-brain** — two nodes both believing they're the leader.
- **KRaft** — Kafka's built-in Raft-based metadata quorum, replacing ZooKeeper.
- **Cache-aside** — the app reads cache first, loads from the DB on a miss, and populates the cache.
