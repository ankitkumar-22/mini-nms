package com.nms.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Setter
@Getter
@Document(collection = "network_metrics")
public class NetworkMetric {

    @Id
    private String id;
    private String deviceId;
    private String ipAddress;
    private double latencyMs;
    private double packetLoss;
    private String status;
    private LocalDateTime timestamp;

    public NetworkMetric() {}

    public NetworkMetric(String deviceId, String ipAddress,
                         double latencyMs, double packetLoss, String status) {
        this.deviceId = deviceId;
        this.ipAddress = ipAddress;
        this.latencyMs = latencyMs;
        this.packetLoss = packetLoss;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

}