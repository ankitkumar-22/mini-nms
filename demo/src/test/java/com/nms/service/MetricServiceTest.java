package com.nms.service;

import com.nms.model.NetworkMetric;
import com.nms.repository.NetworkMetricRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetricServiceTest {

    @Mock
    private NetworkMetricRepository metricRepository;

    @InjectMocks
    private MetricService metricService;

    private NetworkMetric sampleMetric;

    @BeforeEach
    void setUp() {
        sampleMetric = new NetworkMetric("device-123", "8.8.8.8", 12.5, 0.0, "UP");
        sampleMetric.setId("metric-456");
    }

    @Test
    @DisplayName("Should save a new NetworkMetric successfully")
    void saveMetric_Success() {
        when(metricRepository.save(any(NetworkMetric.class))).thenReturn(sampleMetric);

        metricService.saveMetric("device-123", "8.8.8.8", 12.5, 0.0, "UP");

        verify(metricRepository, times(1)).save(any(NetworkMetric.class));
    }

    @Test
    @DisplayName("Should return top 10 metrics for a device ordered by timestamp descending")
    void getLatestMetrics_Success() {
        when(metricRepository.findTop10ByDeviceIdOrderByTimestampDesc("device-123"))
                .thenReturn(List.of(sampleMetric));

        List<NetworkMetric> metrics = metricService.getLatestMetrics("device-123");

        assertThat(metrics).hasSize(1);
        assertThat(metrics.get(0).getDeviceId()).isEqualTo("device-123");
        assertThat(metrics.get(0).getLatencyMs()).isEqualTo(12.5);
    }

    @Test
    @DisplayName("Should return metrics within a specified date-time range")
    void getMetricsByRange_Success() {
        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now();

        when(metricRepository.findByDeviceIdAndTimestampBetweenOrderByTimestampAsc("device-123", start, end))
                .thenReturn(List.of(sampleMetric));

        List<NetworkMetric> metrics = metricService.getMetricsByRange("device-123", start, end);

        assertThat(metrics).hasSize(1);
        verify(metricRepository, times(1))
                .findByDeviceIdAndTimestampBetweenOrderByTimestampAsc("device-123", start, end);
    }

    @Test
    @DisplayName("Should return all metrics for a given device")
    void getAllMetricsForDevice_Success() {
        when(metricRepository.findByDeviceIdOrderByTimestampAsc("device-123")).thenReturn(List.of(sampleMetric));

        List<NetworkMetric> metrics = metricService.getAllMetricsForDevice("device-123");

        assertThat(metrics).hasSize(1);
        verify(metricRepository, times(1)).findByDeviceIdOrderByTimestampAsc("device-123");
    }
}
