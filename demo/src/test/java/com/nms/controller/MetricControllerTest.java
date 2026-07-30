package com.nms.controller;

import com.nms.model.NetworkMetric;
import com.nms.service.MetricService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MetricControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MetricService metricService;

    @InjectMocks
    private MetricController metricController;

    private NetworkMetric sampleMetric;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(metricController).build();

        sampleMetric = new NetworkMetric("device-123", "8.8.8.8", 14.2, 0.0, "UP");
        sampleMetric.setId("metric-001");
    }

    @Test
    @DisplayName("GET /api/metrics/{deviceId}/latest - Should return latest 10 metrics")
    void getLatestMetrics_Success() throws Exception {
        when(metricService.getLatestMetrics("device-123")).thenReturn(List.of(sampleMetric));

        mockMvc.perform(get("/api/metrics/device-123/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].deviceId").value("device-123"))
                .andExpect(jsonPath("$[0].latencyMs").value(14.2))
                .andExpect(jsonPath("$[0].status").value("UP"));
    }

    @Test
    @DisplayName("GET /api/metrics/{deviceId}/all - Should return all metrics for device")
    void getAllMetrics_Success() throws Exception {
        when(metricService.getAllMetricsForDevice("device-123")).thenReturn(List.of(sampleMetric));

        mockMvc.perform(get("/api/metrics/device-123/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].ipAddress").value("8.8.8.8"));
    }

    @Test
    @DisplayName("GET /api/metrics/{deviceId}/range - Should filter metrics by ISO date-time range")
    void getMetricsByRange_Success() throws Exception {
        when(metricService.getMetricsByRange(eq("device-123"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(sampleMetric));

        mockMvc.perform(get("/api/metrics/device-123/range")
                        .param("start", "2026-07-30T00:00:00")
                        .param("end", "2026-07-30T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].deviceId").value("device-123"));
    }
}
