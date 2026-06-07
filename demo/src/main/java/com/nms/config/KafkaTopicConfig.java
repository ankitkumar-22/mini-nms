// src/main/java/com/nms/config/KafkaTopicConfig.java
package com.nms.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic pingJobsTopic() {
        return TopicBuilder.name("ping-jobs").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic pingResultsTopic() {
        return TopicBuilder.name("ping-results").partitions(3).replicas(1).build();
    }
}