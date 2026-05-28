/*

Why two separate DTOs:
DTOPurposeDeviceRequestDTOWhat the client sends — only name and ipAddress, nothing elseDeviceResponseDTOWhat the API returns — full device info including id, status, timestamps
This ensures the client can never accidentally set status or id from outside.
 */

package com.nms.dto;

import java.time.LocalDateTime;

public class DeviceResponseDTO {

    private String id;
    private String name;
    private String ipAddress;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime lastChecked;

    public DeviceResponseDTO() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastChecked() { return lastChecked; }
    public void setLastChecked(LocalDateTime lastChecked) { this.lastChecked = lastChecked; }
}