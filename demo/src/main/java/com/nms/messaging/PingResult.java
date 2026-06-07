// src/main/java/com/nms/messaging/PingResult.java
package com.nms.messaging;

public record PingResult(
        String deviceId,
        String ipAddress,
        String status,
        double latencyMs,
        double packetLoss) {}