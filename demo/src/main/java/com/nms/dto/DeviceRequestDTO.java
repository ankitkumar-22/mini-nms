package com.nms.dto;

public class DeviceRequestDTO {

    private String name;
    private String ipAddress;

    public DeviceRequestDTO() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
}