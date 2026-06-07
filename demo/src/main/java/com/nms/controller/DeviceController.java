package com.nms.controller;

import com.nms.dto.DeviceRequestDTO;
import com.nms.dto.DeviceResponseDTO;
import com.nms.service.DeviceService;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Profile("web")
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping
    public ResponseEntity<DeviceResponseDTO> addDevice(@Valid @RequestBody DeviceRequestDTO request) {
        return new ResponseEntity<>(deviceService.addDevice(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<DeviceResponseDTO>> getAllDevices() {
        return ResponseEntity.ok(deviceService.getAllDevices());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeviceResponseDTO> getDeviceById(@PathVariable String id) {
        return ResponseEntity.ok(deviceService.getDeviceById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDevice(@PathVariable String id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.ok("Device deleted successfully");
    }
}