package com.nms.service;

import com.nms.dto.DeviceRequestDTO;
import com.nms.dto.DeviceResponseDTO;
import com.nms.exception.DeviceNotFoundException;
import com.nms.exception.DuplicateDeviceException;
import com.nms.model.Device;
import com.nms.repository.DeviceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public DeviceResponseDTO addDevice(DeviceRequestDTO request) {
        if (deviceRepository.existsByIpAddress(request.getIpAddress())) {
            throw new DuplicateDeviceException(request.getIpAddress());
        }
        Device device = new Device(request.getName(), request.getIpAddress());
        return mapToResponse(deviceRepository.save(device));
    }

    public List<DeviceResponseDTO> getAllDevices() {
        return deviceRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public DeviceResponseDTO getDeviceById(String id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException(id));
        return mapToResponse(device);
    }

    public void deleteDevice(String id) {
        if (!deviceRepository.existsById(id)) {
            throw new DeviceNotFoundException(id);
        }
        deviceRepository.deleteById(id);
    }

    public void updateDeviceStatus(String id, String status) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException(id));
        device.setStatus(status);
        device.setLastChecked(LocalDateTime.now());
        deviceRepository.save(device);
    }

    private DeviceResponseDTO mapToResponse(Device device) {
        DeviceResponseDTO dto = new DeviceResponseDTO();
        dto.setId(device.getId());
        dto.setName(device.getName());
        dto.setIpAddress(device.getIpAddress());
        dto.setStatus(device.getStatus());
        dto.setCreatedAt(device.getCreatedAt());
        dto.setLastChecked(device.getLastChecked());
        return dto;
    }
}