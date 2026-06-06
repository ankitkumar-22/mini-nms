package com.nms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class DeviceRequestDTO {

    @NotBlank(message = "Device name must not be blank")
    private String name;

    @NotBlank(message = "IP address must not be blank")
    @Pattern(
            regexp = "^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)$",
            message = "IP address must be a valid IPv4 address"
    )
    private String ipAddress;

    public DeviceRequestDTO() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
}