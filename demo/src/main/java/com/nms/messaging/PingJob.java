// src/main/java/com/nms/messaging/PingJob.java
package com.nms.messaging;

public record PingJob(String deviceId, String ipAddress) {}