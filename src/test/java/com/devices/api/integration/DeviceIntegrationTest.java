package com.devices.api.integration;

import com.devices.api.dto.DeviceFullUpdateRequest;
import com.devices.api.dto.DeviceRequest;
import com.devices.api.dto.DeviceResponse;
import com.devices.api.dto.DeviceUpdateRequest;
import com.devices.api.enums.DeviceState;
import com.devices.api.repository.DeviceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Import(DeviceIntegrationTest.TestConfig.class)
class DeviceIntegrationTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        ObjectMapper objectMapper() {
            ObjectMapper mapper = new ObjectMapper();
            mapper.findAndRegisterModules();
            return mapper;
        }
    }

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DeviceRepository deviceRepository;

    @BeforeEach
    void setUp() {
        deviceRepository.deleteAll();
    }

    @Nested
    @DisplayName("Device Lifecycle Tests")
    class DeviceLifecycleTests {

        @Test
        @DisplayName("Should complete full CRUD lifecycle")
        void shouldCompleteCrudLifecycle() throws Exception {
            // Create
            DeviceRequest createRequest = new DeviceRequest("iPhone 15", "Apple", DeviceState.AVAILABLE);

            MvcResult createResult = mockMvc.perform(post("/api/v1/devices")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("iPhone 15"))
                    .andExpect(jsonPath("$.brand").value("Apple"))
                    .andExpect(jsonPath("$.state").value("AVAILABLE"))
                    .andExpect(jsonPath("$.creationTime").exists())
                    .andReturn();

            DeviceResponse created = objectMapper.readValue(
                    createResult.getResponse().getContentAsString(),
                    DeviceResponse.class
            );

            // Read
            mockMvc.perform(get("/api/v1/devices/{id}", created.id()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(created.id().toString()));

            // Update (full replacement via PUT)
            DeviceFullUpdateRequest updateRequest = new DeviceFullUpdateRequest("iPhone 15 Pro", "Apple", DeviceState.AVAILABLE);

            mockMvc.perform(put("/api/v1/devices/{id}", created.id())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("iPhone 15 Pro"))
                    .andExpect(jsonPath("$.state").value("AVAILABLE"));

            // Partial update (set to IN_USE via PATCH)
            DeviceUpdateRequest partialRequest = new DeviceUpdateRequest(null, null, DeviceState.IN_USE);

            mockMvc.perform(patch("/api/v1/devices/{id}", created.id())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(partialRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.state").value("IN_USE"));

            // Change state to allow deletion
            DeviceUpdateRequest stateUpdate = new DeviceUpdateRequest(null, null, DeviceState.INACTIVE);

            mockMvc.perform(patch("/api/v1/devices/{id}", created.id())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(stateUpdate)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.state").value("INACTIVE"));

            // Delete
            mockMvc.perform(delete("/api/v1/devices/{id}", created.id()))
                    .andExpect(status().isNoContent());

            // Verify deleted
            mockMvc.perform(get("/api/v1/devices/{id}", created.id()))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Filtering Tests")
    class FilteringTests {

        @Test
        @DisplayName("Should filter devices by brand")
        void shouldFilterByBrand() throws Exception {
            createDevice("iPhone 15", "Apple", DeviceState.AVAILABLE);
            createDevice("Galaxy S24", "Samsung", DeviceState.AVAILABLE);
            createDevice("MacBook Pro", "Apple", DeviceState.IN_USE);

            mockMvc.perform(get("/api/v1/devices")
                            .param("brand", "Apple"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        @DisplayName("Should filter devices by state")
        void shouldFilterByState() throws Exception {
            createDevice("iPhone 15", "Apple", DeviceState.AVAILABLE);
            createDevice("Galaxy S24", "Samsung", DeviceState.IN_USE);
            createDevice("MacBook Pro", "Apple", DeviceState.AVAILABLE);

            mockMvc.perform(get("/api/v1/devices")
                            .param("state", "AVAILABLE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        @DisplayName("Should filter devices by brand and state")
        void shouldFilterByBrandAndState() throws Exception {
            createDevice("iPhone 15", "Apple", DeviceState.AVAILABLE);
            createDevice("iPhone 14", "Apple", DeviceState.IN_USE);
            createDevice("Galaxy S24", "Samsung", DeviceState.AVAILABLE);

            mockMvc.perform(get("/api/v1/devices")
                            .param("brand", "Apple")
                            .param("state", "AVAILABLE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].name").value("iPhone 15"));
        }

        private void createDevice(String name, String brand, DeviceState state) throws Exception {
            DeviceRequest request = new DeviceRequest(name, brand, state);
            mockMvc.perform(post("/api/v1/devices")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }
    }

    @Nested
    @DisplayName("Business Rules Tests")
    class BusinessRulesTests {

        @Test
        @DisplayName("Should not allow updating name when device is in use")
        void shouldNotAllowUpdatingNameWhenInUse() throws Exception {
            DeviceResponse device = createDeviceWithState(DeviceState.IN_USE);

            DeviceUpdateRequest updateRequest = new DeviceUpdateRequest("New Name", null, null);

            mockMvc.perform(patch("/api/v1/devices/{id}", device.id())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title").value("Device In Use"));
        }

        @Test
        @DisplayName("Should not allow updating brand when device is in use")
        void shouldNotAllowUpdatingBrandWhenInUse() throws Exception {
            DeviceResponse device = createDeviceWithState(DeviceState.IN_USE);

            DeviceUpdateRequest updateRequest = new DeviceUpdateRequest(null, "New Brand", null);

            mockMvc.perform(patch("/api/v1/devices/{id}", device.id())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title").value("Device In Use"));
        }

        @Test
        @DisplayName("Should allow updating state when device is in use")
        void shouldAllowUpdatingStateWhenInUse() throws Exception {
            DeviceResponse device = createDeviceWithState(DeviceState.IN_USE);

            DeviceUpdateRequest updateRequest = new DeviceUpdateRequest(null, null, DeviceState.AVAILABLE);

            mockMvc.perform(patch("/api/v1/devices/{id}", device.id())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.state").value("AVAILABLE"));
        }

        @Test
        @DisplayName("Should not allow deleting device that is in use")
        void shouldNotAllowDeletingInUseDevice() throws Exception {
            DeviceResponse device = createDeviceWithState(DeviceState.IN_USE);

            mockMvc.perform(delete("/api/v1/devices/{id}", device.id()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title").value("Device In Use"));

            // Verify device still exists
            mockMvc.perform(get("/api/v1/devices/{id}", device.id()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should not allow full update when device is in use")
        void shouldNotAllowFullUpdateWhenInUse() throws Exception {
            DeviceResponse device = createDeviceWithState(DeviceState.IN_USE);

            DeviceFullUpdateRequest updateRequest = new DeviceFullUpdateRequest("Updated Name", "Updated Brand", DeviceState.AVAILABLE);

            mockMvc.perform(put("/api/v1/devices/{id}", device.id())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title").value("Device In Use"));
        }

        @Test
        @DisplayName("Should preserve creation time on update")
        void shouldPreserveCreationTimeOnUpdate() throws Exception {
            DeviceResponse device = createDeviceWithState(DeviceState.AVAILABLE);

            DeviceFullUpdateRequest updateRequest = new DeviceFullUpdateRequest("Updated Name", "Updated Brand", DeviceState.INACTIVE);

            MvcResult result = mockMvc.perform(put("/api/v1/devices/{id}", device.id())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andReturn();

            DeviceResponse updated = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    DeviceResponse.class
            );

            assertThat(updated.creationTime()).isEqualTo(device.creationTime());
        }

        private DeviceResponse createDeviceWithState(DeviceState state) throws Exception {
            DeviceRequest request = new DeviceRequest("Test Device", "Test Brand", state);

            MvcResult result = mockMvc.perform(post("/api/v1/devices")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andReturn();

            return objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    DeviceResponse.class
            );
        }
    }
}
