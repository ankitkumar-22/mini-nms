package com.nms.controller;

import com.nms.dto.DeviceRequestDTO;
import com.nms.dto.DeviceResponseDTO;
import com.nms.exception.DeviceNotFoundException;
import com.nms.exception.DuplicateDeviceException;
import com.nms.exception.GlobalExceptionHandler;
import com.nms.service.DeviceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DeviceControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DeviceService deviceService;

    @InjectMocks
    private DeviceController deviceController;

    private DeviceResponseDTO sampleResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(deviceController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sampleResponse = new DeviceResponseDTO();
        sampleResponse.setId("6650a1f2e4b0c8a1d2f3b4c5");
        sampleResponse.setName("Google DNS");
        sampleResponse.setIpAddress("8.8.8.8");
        sampleResponse.setStatus("UNKNOWN");
    }

    @Test
    @DisplayName("POST /api/devices - Should create device and return HTTP 201")
    void createDevice_Success() throws Exception {
        String requestJson = "{\"name\":\"Google DNS\",\"ipAddress\":\"8.8.8.8\"}";

        when(deviceService.addDevice(any(DeviceRequestDTO.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("6650a1f2e4b0c8a1d2f3b4c5"))
                .andExpect(jsonPath("$.name").value("Google DNS"))
                .andExpect(jsonPath("$.ipAddress").value("8.8.8.8"))
                .andExpect(jsonPath("$.status").value("UNKNOWN"));
    }

    @Test
    @DisplayName("POST /api/devices - Should return HTTP 409 when duplicate IP is registered")
    void createDevice_DuplicateIp_Returns409() throws Exception {
        String requestJson = "{\"name\":\"Google DNS\",\"ipAddress\":\"8.8.8.8\"}";

        when(deviceService.addDevice(any(DeviceRequestDTO.class)))
                .thenThrow(new DuplicateDeviceException("8.8.8.8"));

        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("8.8.8.8")));
    }

    @Test
    @DisplayName("GET /api/devices - Should return list of all devices")
    void getAllDevices_Success() throws Exception {
        when(deviceService.getAllDevices()).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Google DNS"));
    }

    @Test
    @DisplayName("GET /api/devices/{id} - Should return device when found")
    void getDeviceById_Success() throws Exception {
        when(deviceService.getDeviceById("6650a1f2e4b0c8a1d2f3b4c5")).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/devices/6650a1f2e4b0c8a1d2f3b4c5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("6650a1f2e4b0c8a1d2f3b4c5"));
    }

    @Test
    @DisplayName("GET /api/devices/{id} - Should return HTTP 404 when device not found")
    void getDeviceById_NotFound_Returns404() throws Exception {
        when(deviceService.getDeviceById("unknown-id"))
                .thenThrow(new DeviceNotFoundException("unknown-id"));

        mockMvc.perform(get("/api/devices/unknown-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("unknown-id")));
    }

    @Test
    @DisplayName("DELETE /api/devices/{id} - Should delete device and return plain text message")
    void deleteDevice_Success() throws Exception {
        doNothing().when(deviceService).deleteDevice("6650a1f2e4b0c8a1d2f3b4c5");

        mockMvc.perform(delete("/api/devices/6650a1f2e4b0c8a1d2f3b4c5"))
                .andExpect(status().isOk())
                .andExpect(content().string("Device deleted successfully"));
    }
}
