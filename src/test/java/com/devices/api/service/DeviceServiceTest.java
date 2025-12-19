package com.devices.api.service;

import com.devices.api.dto.DeviceRequest;
import com.devices.api.dto.DeviceResponse;
import com.devices.api.dto.DeviceUpdateRequest;
import com.devices.api.entity.Device;
import com.devices.api.enums.DeviceState;
import com.devices.api.exception.DeviceInUseException;
import com.devices.api.exception.DeviceNotFoundException;
import com.devices.api.mapper.DeviceMapper;
import com.devices.api.repository.DeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private DeviceMapper deviceMapper;

    @InjectMocks
    private DeviceServiceImpl deviceService;

    private UUID deviceId;
    private Device device;
    private DeviceResponse deviceResponse;

    @BeforeEach
    void setUp() {
        deviceId = UUID.randomUUID();
        device = new Device();
        device.setId(deviceId);
        device.setName("Test Device");
        device.setBrand("Test Brand");
        device.setState(DeviceState.AVAILABLE);
        device.setCreationTime(Instant.now());

        deviceResponse = new DeviceResponse(
                deviceId,
                "Test Device",
                "Test Brand",
                DeviceState.AVAILABLE,
                device.getCreationTime()
        );
    }

    @Nested
    @DisplayName("Create Device Tests")
    class CreateDeviceTests {

        @Test
        @DisplayName("Should create device successfully")
        void shouldCreateDevice() {
            DeviceRequest request = new DeviceRequest("Test Device", "Test Brand", DeviceState.AVAILABLE);

            when(deviceMapper.toEntity(request)).thenReturn(device);
            when(deviceRepository.save(device)).thenReturn(device);
            when(deviceMapper.toResponse(device)).thenReturn(deviceResponse);

            DeviceResponse result = deviceService.create(request);

            assertThat(result).isEqualTo(deviceResponse);
            verify(deviceRepository).save(device);
        }
    }

    @Nested
    @DisplayName("Get Device Tests")
    class GetDeviceTests {

        @Test
        @DisplayName("Should return device when found")
        void shouldReturnDeviceWhenFound() {
            when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
            when(deviceMapper.toResponse(device)).thenReturn(deviceResponse);

            DeviceResponse result = deviceService.getById(deviceId);

            assertThat(result).isEqualTo(deviceResponse);
        }

        @Test
        @DisplayName("Should throw exception when device not found")
        void shouldThrowExceptionWhenNotFound() {
            when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> deviceService.getById(deviceId))
                    .isInstanceOf(DeviceNotFoundException.class)
                    .hasMessageContaining(deviceId.toString());
        }
    }

    @Nested
    @DisplayName("Get All Devices Tests")
    class GetAllDevicesTests {

        @Test
        @DisplayName("Should return all devices when no filters")
        void shouldReturnAllDevices() {
            List<Device> devices = List.of(device);
            List<DeviceResponse> responses = List.of(deviceResponse);

            when(deviceRepository.findAll()).thenReturn(devices);
            when(deviceMapper.toResponseList(devices)).thenReturn(responses);

            List<DeviceResponse> result = deviceService.getAll(null, null);

            assertThat(result).hasSize(1);
            verify(deviceRepository).findAll();
        }

        @Test
        @DisplayName("Should filter by brand")
        void shouldFilterByBrand() {
            List<Device> devices = List.of(device);
            List<DeviceResponse> responses = List.of(deviceResponse);

            when(deviceRepository.findByBrand("Test Brand")).thenReturn(devices);
            when(deviceMapper.toResponseList(devices)).thenReturn(responses);

            List<DeviceResponse> result = deviceService.getAll("Test Brand", null);

            assertThat(result).hasSize(1);
            verify(deviceRepository).findByBrand("Test Brand");
        }

        @Test
        @DisplayName("Should filter by state")
        void shouldFilterByState() {
            List<Device> devices = List.of(device);
            List<DeviceResponse> responses = List.of(deviceResponse);

            when(deviceRepository.findByState(DeviceState.AVAILABLE)).thenReturn(devices);
            when(deviceMapper.toResponseList(devices)).thenReturn(responses);

            List<DeviceResponse> result = deviceService.getAll(null, DeviceState.AVAILABLE);

            assertThat(result).hasSize(1);
            verify(deviceRepository).findByState(DeviceState.AVAILABLE);
        }

        @Test
        @DisplayName("Should filter by brand and state")
        void shouldFilterByBrandAndState() {
            List<Device> devices = List.of(device);
            List<DeviceResponse> responses = List.of(deviceResponse);

            when(deviceRepository.findByBrandAndState("Test Brand", DeviceState.AVAILABLE)).thenReturn(devices);
            when(deviceMapper.toResponseList(devices)).thenReturn(responses);

            List<DeviceResponse> result = deviceService.getAll("Test Brand", DeviceState.AVAILABLE);

            assertThat(result).hasSize(1);
            verify(deviceRepository).findByBrandAndState("Test Brand", DeviceState.AVAILABLE);
        }
    }

    @Nested
    @DisplayName("Update Device Tests")
    class UpdateDeviceTests {

        @Test
        @DisplayName("Should update device successfully when available")
        void shouldUpdateDeviceWhenAvailable() {
            DeviceUpdateRequest request = new DeviceUpdateRequest("Updated Name", "Updated Brand", DeviceState.IN_USE);

            when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
            when(deviceRepository.save(any(Device.class))).thenReturn(device);
            when(deviceMapper.toResponse(device)).thenReturn(deviceResponse);

            DeviceResponse result = deviceService.update(deviceId, request);

            assertThat(result).isNotNull();
            verify(deviceRepository).save(device);
        }

        @Test
        @DisplayName("Should update only state when device is in use")
        void shouldUpdateOnlyStateWhenInUse() {
            device.setState(DeviceState.IN_USE);
            DeviceUpdateRequest request = new DeviceUpdateRequest(null, null, DeviceState.AVAILABLE);

            when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
            when(deviceRepository.save(any(Device.class))).thenReturn(device);
            when(deviceMapper.toResponse(device)).thenReturn(deviceResponse);

            DeviceResponse result = deviceService.update(deviceId, request);

            assertThat(result).isNotNull();
            verify(deviceRepository).save(device);
        }

        @Test
        @DisplayName("Should throw exception when updating name of in-use device")
        void shouldThrowExceptionWhenUpdatingNameOfInUseDevice() {
            device.setState(DeviceState.IN_USE);
            DeviceUpdateRequest request = new DeviceUpdateRequest("New Name", null, null);

            when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

            assertThatThrownBy(() -> deviceService.update(deviceId, request))
                    .isInstanceOf(DeviceInUseException.class)
                    .hasMessageContaining("name or brand");
        }

        @Test
        @DisplayName("Should throw exception when updating brand of in-use device")
        void shouldThrowExceptionWhenUpdatingBrandOfInUseDevice() {
            device.setState(DeviceState.IN_USE);
            DeviceUpdateRequest request = new DeviceUpdateRequest(null, "New Brand", null);

            when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

            assertThatThrownBy(() -> deviceService.update(deviceId, request))
                    .isInstanceOf(DeviceInUseException.class)
                    .hasMessageContaining("name or brand");
        }

        @Test
        @DisplayName("Should throw exception when device not found")
        void shouldThrowExceptionWhenDeviceNotFound() {
            DeviceUpdateRequest request = new DeviceUpdateRequest("Name", "Brand", DeviceState.AVAILABLE);

            when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> deviceService.update(deviceId, request))
                    .isInstanceOf(DeviceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Delete Device Tests")
    class DeleteDeviceTests {

        @Test
        @DisplayName("Should delete device successfully when available")
        void shouldDeleteDeviceWhenAvailable() {
            when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

            deviceService.delete(deviceId);

            verify(deviceRepository).delete(device);
        }

        @Test
        @DisplayName("Should delete device successfully when inactive")
        void shouldDeleteDeviceWhenInactive() {
            device.setState(DeviceState.INACTIVE);
            when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

            deviceService.delete(deviceId);

            verify(deviceRepository).delete(device);
        }

        @Test
        @DisplayName("Should throw exception when deleting in-use device")
        void shouldThrowExceptionWhenDeletingInUseDevice() {
            device.setState(DeviceState.IN_USE);
            when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

            assertThatThrownBy(() -> deviceService.delete(deviceId))
                    .isInstanceOf(DeviceInUseException.class)
                    .hasMessageContaining("in use");

            verify(deviceRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should throw exception when device not found")
        void shouldThrowExceptionWhenDeviceNotFound() {
            when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> deviceService.delete(deviceId))
                    .isInstanceOf(DeviceNotFoundException.class);

            verify(deviceRepository, never()).delete(any());
        }
    }
}
