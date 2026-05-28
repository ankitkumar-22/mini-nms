package com.nms.service;

import com.nms.model.NetworkMetric;
import com.nms.repository.NetworkMetricRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MetricService {

    private final NetworkMetricRepository metricRepository;

    public MetricService(NetworkMetricRepository metricRepository) {
        this.metricRepository = metricRepository;
    }

    public void saveMetric(String deviceId, String ipAddress,
                           double latencyMs, double packetLoss, String status) {
        NetworkMetric metric = new NetworkMetric(deviceId, ipAddress,
                latencyMs, packetLoss, status);
        metricRepository.save(metric);
    }

    public List<NetworkMetric> getLatestMetrics(String deviceId) {
        return metricRepository.findTop10ByDeviceIdOrderByTimestampDesc(deviceId);
    }

    public List<NetworkMetric> getMetricsByRange(String deviceId,
                                                 LocalDateTime start,
                                                 LocalDateTime end) {
        return metricRepository.findByDeviceIdAndTimestampBetween(deviceId, start, end);
    }

    public List<NetworkMetric> getAllMetricsForDevice(String deviceId) {
        return metricRepository.findByDeviceId(deviceId);
    }
}