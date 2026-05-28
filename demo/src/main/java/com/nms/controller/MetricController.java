/*
GET/api/metrics/{deviceId}/latestLast 10 ping results
GET/api/metrics/{deviceId}/allAll metrics for a device
GET/api/metrics/{deviceId}/rangeMetrics between two timestamps
 */
package com.nms.controller;

import com.nms.model.NetworkMetric;
import com.nms.service.MetricService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/metrics")
public class MetricController {

    private final MetricService metricService;

    public MetricController(MetricService metricService) {
        this.metricService = metricService;
    }

    @GetMapping("/{deviceId}/latest")
    public ResponseEntity<List<NetworkMetric>> getLatestMetrics(@PathVariable String deviceId) {
        return ResponseEntity.ok(metricService.getLatestMetrics(deviceId));
    }

    @GetMapping("/{deviceId}/all")
    public ResponseEntity<List<NetworkMetric>> getAllMetrics(@PathVariable String deviceId) {
        return ResponseEntity.ok(metricService.getAllMetricsForDevice(deviceId));
    }

    @GetMapping("/{deviceId}/range")
    public ResponseEntity<List<NetworkMetric>> getMetricsByRange(
            @PathVariable String deviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(metricService.getMetricsByRange(deviceId, start, end));
    }
}