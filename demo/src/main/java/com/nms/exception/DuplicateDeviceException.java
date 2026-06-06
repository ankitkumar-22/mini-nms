package com.nms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateDeviceException extends RuntimeException {
    public DuplicateDeviceException(String ipAddress) {
        super("Device with IP " + ipAddress + " already exists");
    }
}