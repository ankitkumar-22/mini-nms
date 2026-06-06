package com.nms.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Setter
@Getter
@Document(collection = "devices")
public class Device {

    @Id
    private String id;

    @Version
    private Long version;

    private String name;
    private String ipAddress;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime lastChecked;

    public Device() {}

    public Device(String name, String ipAddress) {
        this.name = name;
        this.ipAddress = ipAddress;
        this.status = "UNKNOWN";
        this.createdAt = LocalDateTime.now();
    }

}