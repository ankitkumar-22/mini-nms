package com.nms.service;

import com.nms.dto.DeviceRequestDTO;
import com.nms.dto.DeviceResponseDTO;
import com.nms.exception.DeviceNotFoundException;
import com.nms.exception.DuplicateDeviceException;
import com.nms.model.Device;
import com.nms.repository.DeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @InjectMocks
    private DeviceService deviceService;

    private Device sampleDevice;
    private DeviceRequestDTO sampleRequest;

    @BeforeEach
    void setUp() {
        sampleDevice = new Device("Google DNS", "8.8.8.8");
        sampleDevice.setId("6650a1f2e4b0c8a1d2f3b4c5");

        sampleRequest = new DeviceRequestDTO();
        sampleRequest.setName("Google DNS");
        sampleRequest.setIpAddress("8.8.8.8");
    }

    @Test
    @DisplayName("Should successfully add a new device with UNKNOWN status")
    void addDevice_Success() {
        when(deviceRepository.existsByIpAddress("8.8.8.8")).thenReturn(false);
        when(deviceRepository.save(any(Device.class))).thenReturn(sampleDevice);

        DeviceResponseDTO response = deviceService.addDevice(sampleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("6650a1f2e4b0c8a1d2f3b4c5");
        assertThat(response.getName()).isEqualTo("Google DNS");
        assertThat(response.getIpAddress()).isEqualTo("8.8.8.8");
        assertThat(response.getStatus()).isEqualTo("UNKNOWN");

        verify(deviceRepository, times(1)).existsByIpAddress("8.8.8.8");
        verify(deviceRepository, times(1)).save(any(Device.class));
    }

    @Test
    @DisplayName("Should throw DuplicateDeviceException when IP address already exists")
    void addDevice_DuplicateIp_ThrowsException() {
        when(deviceRepository.existsByIpAddress("8.8.8.8")).thenReturn(true);

        assertThatThrownBy(() -> deviceService.addDevice(sampleRequest))
                .isInstanceOf(DuplicateDeviceException.class)
                .hasMessageContaining("8.8.8.8");

        verify(deviceRepository, times(1)).existsByIpAddress("8.8.8.8");
        verify(deviceRepository, never()).save(any(Device.class));
    }

    @Test
    @DisplayName("Should return all devices")
    void getAllDevices_Success() {
        Device secondDevice = new Device("Cloudflare DNS", "1.1.1.1");
        secondDevice.setId("6650a1f2e4b0c8a1d2f3b4c6");

        when(deviceRepository.findAll()).thenReturn(List.of(sampleDevice, secondDevice));

        List<DeviceResponseDTO> devices = deviceService.getAllDevices();

        assertThat(devices).hasSize(2);
        assertThat(devices.get(0).getName()).isEqualTo("Google DNS");
        assertThat(devices.get(1).getName()).isEqualTo("Cloudflare DNS");
    }

    @Test
    @DisplayName("Should return device by ID when device exists")
    void getDeviceById_Success() {
        when(deviceRepository.findById("6650a1f2e4b0c8a1d2f3b4c5")).thenReturn(Optional.of(sampleDevice));

        DeviceResponseDTO response = deviceService.getDeviceById("6650a1f2e4b0c8a1d2f3b4c5");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("6650a1f2e4b0c8a1d2f3b4c5");
    }

    @Test
    @DisplayName("Should throw DeviceNotFoundException when device ID does not exist")
    void getDeviceById_NotFound_ThrowsException() {
        when(deviceRepository.findById("invalid-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.getDeviceById("invalid-id"))
                .isInstanceOf(DeviceNotFoundException.class)
                .hasMessageContaining("invalid-id");
    }

    @Test
    @DisplayName("Should successfully delete an existing device")
    void deleteDevice_Success() {
        when(deviceRepository.existsById("6650a1f2e4b0c8a1d2f3b4c5")).thenReturn(true);

        deviceService.deleteDevice("6650a1f2e4b0c8a1d2f3b4c5");

        verify(deviceRepository, times(1)).deleteById("6650a1f2e4b0c8a1d2f3b4c5");
    }

    @Test
    @DisplayName("Should update device status and lastChecked timestamp")
    void updateDeviceStatus_Success() {
        when(deviceRepository.findById("6650a1f2e4b0c8a1d2f3b4c5")).thenReturn(Optional.of(sampleDevice));

        deviceService.updateDeviceStatus("6650a1f2e4b0c8a1d2f3b4c5", "UP");

        assertThat(sampleDevice.getStatus()).isEqualTo("UP");
        assertThat(sampleDevice.getLastChecked()).isNotNull();
        verify(deviceRepository, times(1)).save(sampleDevice);
    }
}
