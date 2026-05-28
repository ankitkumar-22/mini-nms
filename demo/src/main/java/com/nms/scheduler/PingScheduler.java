package com.nms.scheduler;

import com.nms.model.Device;
import com.nms.repository.DeviceRepository;
import com.nms.service.MetricService;
import com.nms.service.PingService;
import com.nms.service.PingService.PingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class PingScheduler {

    private static final Logger log = LoggerFactory.getLogger(PingScheduler.class);

    private final DeviceRepository deviceRepository;
    private final PingService pingService;
    private final MetricService metricService;

    public PingScheduler(DeviceRepository deviceRepository,
                         PingService pingService,
                         MetricService metricService) {
        this.deviceRepository = deviceRepository;
        this.pingService = pingService;
        this.metricService = metricService;
    }

    @Scheduled(fixedRate = 60000) // runs every 60 seconds
    public void pingAllDevices() {
        List<Device> devices = deviceRepository.findAll();

        if (devices.isEmpty()) {
            log.info("[{}] No devices registered. Skipping ping cycle.", LocalDateTime.now());
            return;
        }

        log.info("[{}] Starting ping cycle for {} device(s).", LocalDateTime.now(), devices.size());

        for (Device device : devices) {
            try {
                PingResult result = pingService.ping(device.getIpAddress());

                // Update device status in MongoDB
                device.setStatus(result.getStatus());
                device.setLastChecked(LocalDateTime.now());
                deviceRepository.save(device);

                // Save metric snapshot
                metricService.saveMetric(
                        device.getId(),
                        device.getIpAddress(),
                        result.getLatencyMs(),
                        result.getPacketLoss(),
                        result.getStatus()
                );

                log.info("Device: {} | IP: {} | Status: {} | Latency: {}ms | Packet Loss: {}%",
                        device.getName(),
                        device.getIpAddress(),
                        result.getStatus(),
                        result.getLatencyMs(),
                        result.getPacketLoss());

            } catch (Exception e) {
                log.error("Failed to ping device {} ({}): {}",
                        device.getName(), device.getIpAddress(), e.getMessage());
            }
        }

        log.info("[{}] Ping cycle complete.", LocalDateTime.now());
    }
}