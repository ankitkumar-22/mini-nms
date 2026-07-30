// src/main/java/com/nms/ingestion/ResultIngestionConsumer.java
package com.nms.ingestion;

import com.nms.messaging.PingResult;
import com.nms.model.Device;
import com.nms.service.MetricService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Profile("ingestion")
public class ResultIngestionConsumer {

    private static final Logger log = LoggerFactory.getLogger(ResultIngestionConsumer.class);

    private final MongoTemplate mongoTemplate;
    private final MetricService metricService;

    public ResultIngestionConsumer(MongoTemplate mongoTemplate,
                                   MetricService metricService) {
        this.mongoTemplate = mongoTemplate;
        this.metricService = metricService;
    }

    @KafkaListener(topics = "ping-results", groupId = "ingestion-group")
    public void handle(PingResult result) {
        mongoTemplate.updateFirst(
                Query.query(Criteria.where("id").is(result.deviceId())),
                new Update()
                        .set("status", result.status())
                        .set("lastChecked", LocalDateTime.now()),
                Device.class);

        metricService.saveMetric(
                result.deviceId(), result.ipAddress(),
                result.latencyMs(), result.packetLoss(), result.status());

        log.info("Ingested {} -> {} ({} ms)", result.deviceId(), result.status(), result.latencyMs());
    }
}