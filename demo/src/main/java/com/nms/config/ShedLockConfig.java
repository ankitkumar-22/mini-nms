package com.nms.config;

import com.mongodb.client.MongoDatabase;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.mongo.MongoLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
@Profile("scheduler")
@EnableSchedulerLock(defaultLockAtMostFor = "PT50S")
public class ShedLockConfig {

    @Bean
    public LockProvider lockProvider(MongoTemplate mongoTemplate) {
        MongoDatabase db = mongoTemplate.getDb();
        return new MongoLockProvider(db);
    }
}