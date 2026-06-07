// src/main/java/com/nms/worker/PingWorker.java
package com.nms.worker;

import com.nms.messaging.PingJob;
import com.nms.messaging.PingResult;
import com.nms.service.PingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.kafka.annotation.EnableKafka;

@Component
@Profile("worker")
@EnableKafka
public class PingWorker {

    private static final Logger log = LoggerFactory.getLogger(PingWorker.class);

    private final PingService pingService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PingWorker(PingService pingService,
                      KafkaTemplate<String, Object> kafkaTemplate) {
        this.pingService = pingService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "ping-jobs", groupId = "ping-worker-group")
    public void handle(PingJob job) {
        PingService.PingResult r = pingService.ping(job.ipAddress());

        PingResult result = new PingResult(
                job.deviceId(), job.ipAddress(),
                r.getStatus(), r.getLatencyMs(), r.getPacketLoss());

        kafkaTemplate.send("ping-results", job.deviceId(), result);

        log.info("Pinged {} -> {} ({}ms, {}% loss)",
                job.ipAddress(), r.getStatus(), r.getLatencyMs(), r.getPacketLoss());
    }
}