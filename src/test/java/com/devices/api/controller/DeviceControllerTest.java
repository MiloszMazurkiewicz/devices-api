package com.devices.api.controller;

import com.devices.api.dto.DeviceFullUpdateRequest;
import com.devices.api.dto.DeviceRequest;
import com.devices.api.dto.DeviceResponse;
import com.devices.api.dto.DeviceUpdateRequest;
import com.devices.api.enums.DeviceState;
import com.devices.api.exception.DeviceInUseException;
import com.devices.api.exception.DeviceNotFoundException;
import com.devices.api.exception.GlobalExceptionHandler;
import com.devices.api.service.DeviceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DeviceController.class)
@Import({GlobalExceptionHandler.class, DeviceControllerTest.TestConfig.class})
class DeviceControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        DeviceService deviceService() {
            return mock(DeviceService.class);
        }

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DeviceService deviceService;

    private UUID deviceId;
    private DeviceResponse deviceResponse;

    @BeforeEach
    void setUp() {
        reset(deviceService);
        deviceId = UUID.randomUUID();
        deviceResponse = new DeviceResponse(
                deviceId,
                "Test Device",
                "Test Brand",
                DeviceState.AVAILABLE,
                Instant.now()
        );
    }

    @Nested
    @DisplayName("POST /api/v1/devices")
    class CreateDeviceTests {

        @Test
        @DisplayName("Should create device and return 201")
        void shouldCreateDevice() throws Exception {
            DeviceRequest request = new DeviceRequest("Test Device", "Test Brand", DeviceState.AVAILABLE);

            when(deviceService.create(any(DeviceRequest.class))).thenReturn(deviceResponse);

            mockMvc.perform(post("/api/v1/devices")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(deviceId.toString()))
                    .andExpect(jsonPath("$.name").value("Test Device"))
                    .andExpect(jsonPath("$.brand").value("Test Brand"))
                    .andExpect(jsonPath("$.state").value("AVAILABLE"));
        }

        @Test
        @DisplayName("Should return 400 when name is blank")
        void shouldReturn400WhenNameIsBlank() throws Exception {
            DeviceRequest request = new DeviceRequest("", "Test Brand", DeviceState.AVAILABLE);

            mockMvc.perform(post("/api/v1/devices")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Validation Error"));
        }

        @Test
        @DisplayName("Should return 400 when brand is missing")
        void shouldReturn400WhenBrandIsMissing() throws Exception {
            String json = "{\"name\": \"Test\", \"state\": \"AVAILABLE\"}";

            mockMvc.perform(post("/api/v1/devices")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when state is missing")
        void shouldReturn400WhenStateIsMissing() throws Exception {
            String json = "{\"name\": \"Test\", \"brand\": \"Brand\"}";

            mockMvc.perform(post("/api/v1/devices")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/devices/{id}")
    class GetDeviceTests {

        @Test
        @DisplayName("Should return device when found")
        void shouldReturnDevice() throws Exception {
            when(deviceService.getById(deviceId)).thenReturn(deviceResponse);

            mockMvc.perform(get("/api/v1/devices/{id}", deviceId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(deviceId.toString()))
                    .andExpect(jsonPath("$.name").value("Test Device"));
        }

        @Test
        @DisplayName("Should return 404 when device not found")
        void shouldReturn404WhenNotFound() throws Exception {
            when(deviceService.getById(deviceId)).thenThrow(new DeviceNotFoundException(deviceId));

            mockMvc.perform(get("/api/v1/devices/{id}", deviceId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Device Not Found"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/devices")
    class GetAllDevicesTests {

        @Test
        @DisplayName("Should return all devices")
        void shouldReturnAllDevices() throws Exception {
            when(deviceService.getAll(null, null)).thenReturn(List.of(deviceResponse));

            mockMvc.perform(get("/api/v1/devices"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(deviceId.toString()));
        }

        @Test
        @DisplayName("Should filter by brand")
        void shouldFilterByBrand() throws Exception {
            when(deviceService.getAll("Test Brand", null)).thenReturn(List.of(deviceResponse));

            mockMvc.perform(get("/api/v1/devices")
                            .param("brand", "Test Brand"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].brand").value("Test Brand"));

            verify(deviceService).getAll("Test Brand", null);
        }

        @Test
        @DisplayName("Should filter by state")
        void shouldFilterByState() throws Exception {
            when(deviceService.getAll(null, DeviceState.AVAILABLE)).thenReturn(List.of(deviceResponse));

            mockMvc.perform(get("/api/v1/devices")
                            .param("state", "AVAILABLE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].state").value("AVAILABLE"));

            verify(deviceService).getAll(null, DeviceState.AVAILABLE);
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/devices/{id}")
    class UpdateDeviceTests {

        @Test
        @DisplayName("Should fully update device successfully")
        void shouldUpdateDevice() throws Exception {
            DeviceFullUpdateRequest request = new DeviceFullUpdateRequest("Updated", "Brand", DeviceState.IN_USE);

            when(deviceService.update(eq(deviceId), any(DeviceFullUpdateRequest.class))).thenReturn(deviceResponse);

            mockMvc.perform(put("/api/v1/devices/{id}", deviceId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should return 400 when name is missing")
        void shouldReturn400WhenNameIsMissing() throws Exception {
            String json = "{\"brand\": \"Apple\", \"state\": \"AVAILABLE\"}";

            mockMvc.perform(put("/api/v1/devices/{id}", deviceId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when brand is missing")
        void shouldReturn400WhenBrandIsMissing() throws Exception {
            String json = "{\"name\": \"iPhone\", \"state\": \"AVAILABLE\"}";

            mockMvc.perform(put("/api/v1/devices/{id}", deviceId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when state is missing")
        void shouldReturn400WhenStateIsMissing() throws Exception {
            String json = "{\"name\": \"iPhone\", \"brand\": \"Apple\"}";

            mockMvc.perform(put("/api/v1/devices/{id}", deviceId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 409 when device is in use")
        void shouldReturn409WhenDeviceInUse() throws Exception {
            DeviceFullUpdateRequest request = new DeviceFullUpdateRequest("Updated", "Brand", DeviceState.AVAILABLE);

            when(deviceService.update(eq(deviceId), any(DeviceFullUpdateRequest.class)))
                    .thenThrow(new DeviceInUseException("Cannot fully update device that is in use"));

            mockMvc.perform(put("/api/v1/devices/{id}", deviceId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title").value("Device In Use"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/devices/{id}")
    class PartialUpdateDeviceTests {

        @Test
        @DisplayName("Should partially update device successfully")
        void shouldPartiallyUpdateDevice() throws Exception {
            DeviceUpdateRequest request = new DeviceUpdateRequest(null, null, DeviceState.INACTIVE);

            when(deviceService.partialUpdate(eq(deviceId), any(DeviceUpdateRequest.class))).thenReturn(deviceResponse);

            mockMvc.perform(patch("/api/v1/devices/{id}", deviceId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/devices/{id}")
    class DeleteDeviceTests {

        @Test
        @DisplayName("Should delete device and return 204")
        void shouldDeleteDevice() throws Exception {
            doNothing().when(deviceService).delete(deviceId);

            mockMvc.perform(delete("/api/v1/devices/{id}", deviceId))
                    .andExpect(status().isNoContent());

            verify(deviceService).delete(deviceId);
        }

        @Test
        @DisplayName("Should return 404 when device not found")
        void shouldReturn404WhenNotFound() throws Exception {
            doThrow(new DeviceNotFoundException(deviceId)).when(deviceService).delete(deviceId);

            mockMvc.perform(delete("/api/v1/devices/{id}", deviceId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 409 when device is in use")
        void shouldReturn409WhenDeviceInUse() throws Exception {
            doThrow(new DeviceInUseException("Cannot delete")).when(deviceService).delete(deviceId);

            mockMvc.perform(delete("/api/v1/devices/{id}", deviceId))
                    .andExpect(status().isConflict());
        }
    }
}
