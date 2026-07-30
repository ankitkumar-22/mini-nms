# Mini-NMS — Frontend Design Brief

> Feed this document to Google Stitch (or any frontend designer). It describes everything the
> UI needs to know about the backend: the data shapes, the API contract, sample data, which
> endpoint drives which chart, the screens to design, and the integration gotchas to design around.

---

## 1. What this system is (30-second version)

Mini-NMS is a small **Network Monitoring System**. You register network devices (by IP). Every
60 seconds the backend pings each one, records whether it is **UP** or **DOWN**, its **latency**,
and its **packet loss**, and stores each result as a time-series point in MongoDB. The frontend's
job is to let a user **add/remove devices**, **see current status at a glance**, **drill into a
device's history as charts**, and **watch the internal Kafka pipeline** that does the work.

### The pipeline (why it matters for the UI)

Work flows through Kafka in three stages. This is what the "Pipeline" screen visualizes:

```
 PingScheduler            ping-jobs topic          PingWorker              ping-results topic       ResultIngestionConsumer
 (every 60s)      ──▶     (3 partitions)     ──▶   (runs the real ping)  ──▶  (3 partitions)   ──▶   (writes to MongoDB)
 1 job per device         keyed by deviceId        4 pings per device      keyed by deviceId         updates device status
                                                                                                     + appends a metric row
```

The app runs as separate **profiles** (`scheduler`, `worker`, `ingestion`, `web`) that can be
scaled independently — this is the "increase consumers if a stage is a bottleneck" story in §8.

---

## 2. Core data objects (with real sample JSON)

There are exactly two things the UI deals with: **Device** and **NetworkMetric**.

### 2.1 Device — one registered machine being monitored

```json
{
  "id": "6650a1f2e4b0c8a1d2f3b4c5",
  "name": "Google DNS",
  "ipAddress": "8.8.8.8",
  "status": "UP",
  "createdAt": "2026-07-30T09:14:22.481",
  "lastChecked": "2026-07-30T11:42:00.017",
  "version": 7
}
```

| Field         | Type                | Notes for the UI |
|---------------|---------------------|------------------|
| `id`          | string (Mongo ObjectId, 24 hex chars) | The device key used in every metrics URL. **Example: `6650a1f2e4b0c8a1d2f3b4c5`.** Never editable by the user. |
| `name`        | string              | User-supplied label. Shown as the card/row title. |
| `ipAddress`   | string (IPv4)       | **Example: `8.8.8.8`.** IPv4 only, validated server-side (see §4.1). |
| `status`      | enum string         | One of **`UP`**, **`DOWN`**, **`UNKNOWN`**. `UNKNOWN` = registered but not yet pinged. Design a color for each (e.g. green / red / grey). |
| `createdAt`   | timestamp (no zone) | When the device was added. |
| `lastChecked` | timestamp or `null`  | Last ping time. **`null` until the first ping completes** — design an empty state ("Never"). |
| `version`     | number              | Internal optimistic-lock counter. **Ignore in the UI.** |

### 2.2 NetworkMetric — one ping result (the time-series / chart row)

```json
{
  "id": "6650b077e4b0c8a1d2f3b501",
  "deviceId": "6650a1f2e4b0c8a1d2f3b4c5",
  "ipAddress": "8.8.8.8",
  "latencyMs": 12.4,
  "packetLoss": 0.0,
  "status": "UP",
  "timestamp": "2026-07-30T11:42:00.017"
}
```

| Field        | Type   | Notes for the UI |
|--------------|--------|------------------|
| `id`         | string | Row id, not shown. |
| `deviceId`   | string | Which device this belongs to (matches `Device.id`). |
| `ipAddress`  | string | Snapshot of the IP at ping time. |
| `latencyMs`  | number | Average round-trip in ms. **`-1` means the ping failed** — plot as a gap / null, not as a real value. |
| `packetLoss` | number | Percentage `0`–`100`. |
| `status`     | string | `UP` / `DOWN` for that single ping. |
| `timestamp`  | timestamp (no zone) | X-axis value for every chart. One row per 60s cycle per device. |

---

## 3. How the data is stored in MongoDB (chart-readiness)

- Database: **`nms_db`**
- Collection **`devices`** — one document per device (the §2.1 shape).
- Collection **`network_metrics`** — **append-only time series**, one document per ping per device
  (the §2.2 shape). This grows ~1 row/device/minute (≈1,440 rows/device/day).

**Why this is already chart-ready:** every metric row carries `deviceId` + `timestamp` + the three
numeric series (`latencyMs`, `packetLoss`, and a derivable `up/down`). To draw any chart you just
pull a device's rows for a time window and map `timestamp → x`, `<metric> → y`. No server-side
aggregation exists yet, so the frontend does its own bucketing/averaging if it wants smoother lines.

---

## 4. REST API contract

Base URL (local dev): **`http://localhost:8080`**. All bodies are JSON. **No authentication.**

### 4.1 Devices — `/api/devices`

| Method & path              | Purpose | Request body | Success |
|----------------------------|---------|--------------|---------|
| `POST /api/devices`        | Register a device | `{ "name": "...", "ipAddress": "..." }` | `201 Created` + Device |
| `GET /api/devices`         | List all devices (dashboard source) | — | `200 OK` + Device[] |
| `GET /api/devices/{id}`    | One device | — | `200 OK` + Device |
| `DELETE /api/devices/{id}` | Remove a device | — | `200 OK` + `"Device deleted successfully"` (plain text) |

**Create request:**
```json
{ "name": "Cloudflare DNS", "ipAddress": "1.1.1.1" }
```
Validation (both required):
- `name` — must not be blank.
- `ipAddress` — must be a valid **IPv4** address (`0.0.0.0`–`255.255.255.255`).

New devices come back with `status: "UNKNOWN"` and `lastChecked: null` until the first ping runs
(up to ~60s later). Design for that transient state.

### 4.2 Metrics / history — `/api/metrics`

| Method & path                                   | Purpose | Returns |
|-------------------------------------------------|---------|---------|
| `GET /api/metrics/{deviceId}/latest`            | **Last 10** ping results, newest first | NetworkMetric[] |
| `GET /api/metrics/{deviceId}/all`               | **Every** metric for the device | NetworkMetric[] |
| `GET /api/metrics/{deviceId}/range?start=&end=` | Metrics between two timestamps | NetworkMetric[] |

`start` / `end` are ISO-8601 **local** date-times, e.g.:
```
GET /api/metrics/6650a1f2e4b0c8a1d2f3b4c5/range?start=2026-07-30T00:00:00&end=2026-07-30T23:59:59
```

⚠️ `/latest` is sorted **newest-first** — reverse it before feeding a left-to-right time chart.
⚠️ `/all` is **unbounded** and grows forever; use `/range` for real charts and keep `/all` for small/demo devices only.

### 4.3 Error shape (all endpoints)

Errors are uniform JSON — design one reusable error/toast component around this:
```json
{
  "timestamp": "2026-07-30T11:45:10.220",
  "status": 404,
  "error": "Not Found",
  "message": "Device not found with id: 6650a1f2e4b0c8a1d2f3b4c5"
}
```
Validation failures add a `fieldErrors` map (drive inline form errors from it):
```json
{
  "timestamp": "2026-07-30T11:45:10.220",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "fieldErrors": { "ipAddress": "IP address must be a valid IPv4 address" }
}
```
Status codes you must handle: **201** (created), **200** (ok), **400** (validation), **404** (missing device), **409** (duplicate IP / concurrent edit), **500** (server).

---

## 5. Which endpoint drives which chart

| Visualization | Source | Mapping |
|---------------|--------|---------|
| **Status badge** (per device) | `Device.status` from `GET /api/devices` | color-map UP/DOWN/UNKNOWN |
| **Latency line chart** | `/range` (or `/latest` reversed) | x = `timestamp`, y = `latencyMs` (drop/`null` where `-1`) |
| **Packet-loss chart** | same rows | x = `timestamp`, y = `packetLoss` (0–100%) |
| **Status timeline / uptime bar** | same rows | color each `timestamp` slot by `status` |
| **Uptime %** (KPI) | same rows | `count(status==UP) / count(*)` over the window |
| **Fleet summary** (dashboard header) | `GET /api/devices` | count devices by `status` |

---

## 6. Integration gotchas — design around these

1. **CORS is not enabled yet.** A Stitch frontend on a different origin (e.g. `localhost:3000`) will
   be blocked until the backend adds CORS. This is a backend to-do (see §9) — assume same-origin or
   a dev proxy while designing.
2. **No auth / no user accounts.** Don't design login, roles, or per-user data.
3. **Timestamps have no timezone.** They are server-local ISO strings (`2026-07-30T11:42:00.017`).
   Don't apply an offset; label charts as server time.
4. **`latencyMs == -1`** is a failed ping sentinel — render as a gap, never as a "-1 ms" point.
5. **`lastChecked` can be `null`** and `status` can be `UNKNOWN` for freshly added devices.
6. **First data is delayed** up to ~60s after adding a device (the scheduler runs on a 60s cycle).
7. **DELETE returns plain text**, not JSON — don't `JSON.parse` that one response.
8. **No pagination anywhere.** `/api/devices` and `/all` return full lists; handle large arrays client-side.

---

## 7. Screens to design (Stitch targets)

### 7.1 Device Dashboard (primary)
Grid of device cards (or a table). Each card: `name`, `ipAddress`, a **status badge**
(UP=green / DOWN=red / UNKNOWN=grey), and `lastChecked` (or "Never"). A header strip with fleet
KPIs (total, # UP, # DOWN). Source: `GET /api/devices`. Clicking a card drills into history charts
(§5) via `/api/metrics/{id}/range` — treat this drill-down as a lightweight expansion, not a full
separate app section.

### 7.2 Add / Manage Devices
A form with two fields: **Name** (text) and **IP address** (IPv4). Show inline validation errors
driven by the `fieldErrors` map (§4.3), and a distinct message on **409 Conflict** ("A device with
this IP already exists"). Each device row/card also needs a **Delete** action with a confirm step.
Sources: `POST /api/devices`, `DELETE /api/devices/{id}`.

### 7.3 Pipeline / Kafka Monitor (educational)
A screen that visualizes the flow in §1 as three stages (Scheduler → Worker → Ingestion) with the
two topics between them. See §8 for exactly what data is (and isn't) available to make this live.

---

## 8. Kafka pipeline visibility & scaling (the educational goal)

**Chosen approach: an off-the-shelf Kafka console container** (recommended for teaching — zero
backend code, shows topics, partitions, consumer-group lag, and throughput live). Add this to your
Docker setup alongside Kafka:

```yaml
  kafka-ui:
    image: kafbat/kafka-ui:latest      # or provectuslabs/kafka-ui, or redpanda console
    container_name: nms-kafka-ui
    ports:
      - "8085:8080"
    environment:
      KAFKA_CLUSTERS_0_NAME: mini-nms
      KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:9092
    depends_on:
      - kafka
```
Open **http://localhost:8085** to watch messages flowing through `ping-jobs` and `ping-results`,
and to read **consumer-group lag** for `ping-worker-group` and `ingestion-group`.

### What "load" looks like and when to scale
- Each topic has **3 partitions**. A consumer **group** can have **up to 3 active consumers** — each
  owns one partition. A 4th consumer in the same group sits **idle**.
- **Lag** (messages produced but not yet consumed, shown in the console) is the bottleneck signal.
  Rising lag on `ping-worker-group` → workers can't keep up → add worker capacity.
- **Two levers to add consumers:**
  1. Run more instances of the `worker` profile (they auto-join `ping-worker-group` and split partitions), **or**
  2. Raise in-JVM concurrency: `factory.setConcurrency(3)` in `KafkaConfig` so one worker uses all 3 partitions.
- To scale **beyond 3** consumers, first **increase the topic's partition count** (in `KafkaTopicConfig`).

### Note on the designed Pipeline screen (§7.3)
The live lag/throughput numbers live **inside the Kafka console container**, not in the backend API —
there is currently **no `/api/pipeline` JSON endpoint**. So design the Stitch pipeline screen as an
**explanatory flow diagram** (three stages + two topics + a legend explaining partitions/lag/scaling),
with a button/embed that **links out to the console at :8085** for the real numbers. 👉 If you later
want lag rendered natively in your own UI, the backend needs a small addition: a Spring Actuator +
Micrometer endpoint exposing consumer-group lag as JSON. Flag that as a follow-up; it is not built yet.

---

## 9. Pre-build checklist (backend to-dos + how to run)

**Backend must-dos before the frontend can talk to it:**
- [ ] **Add CORS** (`@CrossOrigin` or a `WebMvcConfigurer`) allowing the frontend origin — otherwise every request is blocked (§6.1).
- [ ] **Seed demo data** so charts aren't empty on first load (see appendix).
- [ ] (Optional) Add the `/api/pipeline` lag endpoint only if you want native pipeline numbers (§8).
- [ ] (Optional) Cap `/all` or add pagination before the metrics collection grows large.

**Run it locally:**
```bash
# 1. Infra
docker compose -f demo/kafka-dev.yml up -d          # Kafka
docker run -d -p 27017:27017 --name nms-mongo mongo:8.0   # MongoDB
# (add the kafka-ui service from §8 to watch the pipeline)

# 2. App — all stages in one JVM for dev:
cd demo && ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

**Smoke test the contract (do this before wiring the UI):**
```bash
# add a device
curl -s -X POST localhost:8080/api/devices \
  -H 'Content-Type: application/json' \
  -d '{"name":"Google DNS","ipAddress":"8.8.8.8"}'

# list devices (dashboard data)
curl -s localhost:8080/api/devices

# wait ~60s for the first ping, then history for charts:
curl -s localhost:8080/api/metrics/<DEVICE_ID>/latest
```

---

## Appendix A — sample identifiers (copy/paste for mockups)

| Thing | Sample value |
|-------|--------------|
| Device id | `6650a1f2e4b0c8a1d2f3b4c5` |
| Another device id | `6650a1f2e4b0c8a1d2f3b4d0` |
| Device IPs | `8.8.8.8` (Google DNS), `1.1.1.1` (Cloudflare), `192.168.1.1` (Gateway) |
| Statuses | `UP`, `DOWN`, `UNKNOWN` |
| Timestamp format | `2026-07-30T11:42:00.017` (ISO-8601, no zone) |

## Appendix B — curl cookbook

```bash
# Create
curl -X POST localhost:8080/api/devices -H 'Content-Type: application/json' \
  -d '{"name":"Cloudflare DNS","ipAddress":"1.1.1.1"}'

# List all
curl localhost:8080/api/devices

# One device
curl localhost:8080/api/devices/6650a1f2e4b0c8a1d2f3b4c5

# Delete
curl -X DELETE localhost:8080/api/devices/6650a1f2e4b0c8a1d2f3b4c5

# Latest 10 metrics (newest first — reverse for charts)
curl localhost:8080/api/metrics/6650a1f2e4b0c8a1d2f3b4c5/latest

# Range (charts)
curl "localhost:8080/api/metrics/6650a1f2e4b0c8a1d2f3b4c5/range?start=2026-07-30T00:00:00&end=2026-07-30T23:59:59"
```

## Appendix C — sample response arrays (for mockup data)

`GET /api/devices`:
```json
[
  { "id": "6650a1f2e4b0c8a1d2f3b4c5", "name": "Google DNS",   "ipAddress": "8.8.8.8",     "status": "UP",      "createdAt": "2026-07-30T09:14:22.481", "lastChecked": "2026-07-30T11:42:00.017", "version": 7 },
  { "id": "6650a1f2e4b0c8a1d2f3b4d0", "name": "Cloudflare DNS","ipAddress": "1.1.1.1",     "status": "DOWN",    "createdAt": "2026-07-30T09:15:01.002", "lastChecked": "2026-07-30T11:42:00.190", "version": 5 },
  { "id": "6650a1f2e4b0c8a1d2f3b4e1", "name": "Office Gateway","ipAddress": "192.168.1.1", "status": "UNKNOWN", "createdAt": "2026-07-30T11:41:55.900", "lastChecked": null, "version": 0 }
]
```

`GET /api/metrics/{deviceId}/latest` (newest first):
```json
[
  { "id": "6650b077e4b0c8a1d2f3b501", "deviceId": "6650a1f2e4b0c8a1d2f3b4c5", "ipAddress": "8.8.8.8", "latencyMs": 12.4, "packetLoss": 0.0,   "status": "UP",   "timestamp": "2026-07-30T11:42:00.017" },
  { "id": "6650b03be4b0c8a1d2f3b4f8", "deviceId": "6650a1f2e4b0c8a1d2f3b4c5", "ipAddress": "8.8.8.8", "latencyMs": 11.9, "packetLoss": 0.0,   "status": "UP",   "timestamp": "2026-07-30T11:41:00.010" },
  { "id": "6650b000e4b0c8a1d2f3b4ef", "deviceId": "6650a1f2e4b0c8a1d2f3b4c5", "ipAddress": "8.8.8.8", "latencyMs": -1,   "packetLoss": 100.0, "status": "DOWN", "timestamp": "2026-07-30T11:40:00.005" }
]
```
