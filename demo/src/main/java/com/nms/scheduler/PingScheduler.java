// src/main/java/com/nms/scheduler/PingScheduler.java
package com.nms.scheduler;

import com.nms.messaging.PingJob;
import com.nms.model.Device;
import com.nms.repository.DeviceRepository;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("scheduler")
public class PingScheduler {

    private static final Logger log = LoggerFactory.getLogger(PingScheduler.class);

    private final DeviceRepository deviceRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PingScheduler(DeviceRepository deviceRepository,
                         KafkaTemplate<String, Object> kafkaTemplate) {
        this.deviceRepository = deviceRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedRate = 60000)
    @SchedulerLock(name = "pingDispatch", lockAtMostFor = "PT50S", lockAtLeastFor = "PT5S")
    public void dispatchPingJobs() {
        List<Device> devices = deviceRepository.findAll();
        if (devices.isEmpty()) {
            log.info("No devices registered. Nothing to dispatch.");
            return;
        }
        for (Device device : devices) {
            kafkaTemplate.send("ping-jobs", device.getId(),
                    new PingJob(device.getId(), device.getIpAddress()));
        }
        log.info("Dispatched {} ping job(s).", devices.size());
    }
}