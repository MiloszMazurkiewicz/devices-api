package com.devices.api.repository;

import com.devices.api.entity.Device;
import com.devices.api.enums.DeviceState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DeviceRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private DeviceRepository deviceRepository;

    @BeforeEach
    void setUp() {
        deviceRepository.deleteAll();
    }

    @Test
    @DisplayName("Should save device with auto-generated creation time")
    void shouldSaveDeviceWithCreationTime() {
        Device device = new Device();
        device.setName("Test Device");
        device.setBrand("Test Brand");
        device.setState(DeviceState.AVAILABLE);

        Device saved = deviceRepository.save(device);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreationTime()).isNotNull();
    }

    @Test
    @DisplayName("Should find devices by brand")
    void shouldFindByBrand() {
        createDevice("iPhone 15", "Apple", DeviceState.AVAILABLE);
        createDevice("Galaxy S24", "Samsung", DeviceState.AVAILABLE);
        createDevice("MacBook Pro", "Apple", DeviceState.IN_USE);

        List<Device> appleDevices = deviceRepository.findByBrand("Apple");

        assertThat(appleDevices).hasSize(2);
        assertThat(appleDevices).allMatch(d -> d.getBrand().equals("Apple"));
    }

    @Test
    @DisplayName("Should find devices by state")
    void shouldFindByState() {
        createDevice("iPhone 15", "Apple", DeviceState.AVAILABLE);
        createDevice("Galaxy S24", "Samsung", DeviceState.IN_USE);
        createDevice("MacBook Pro", "Apple", DeviceState.AVAILABLE);

        List<Device> availableDevices = deviceRepository.findByState(DeviceState.AVAILABLE);

        assertThat(availableDevices).hasSize(2);
        assertThat(availableDevices).allMatch(d -> d.getState() == DeviceState.AVAILABLE);
    }

    @Test
    @DisplayName("Should find devices by brand and state")
    void shouldFindByBrandAndState() {
        createDevice("iPhone 15", "Apple", DeviceState.AVAILABLE);
        createDevice("iPhone 14", "Apple", DeviceState.IN_USE);
        createDevice("Galaxy S24", "Samsung", DeviceState.AVAILABLE);

        List<Device> devices = deviceRepository.findByBrandAndState("Apple", DeviceState.AVAILABLE);

        assertThat(devices).hasSize(1);
        assertThat(devices.get(0).getName()).isEqualTo("iPhone 15");
    }

    @Test
    @DisplayName("Should return empty list when no devices match brand")
    void shouldReturnEmptyWhenNoBrandMatch() {
        createDevice("iPhone 15", "Apple", DeviceState.AVAILABLE);

        List<Device> devices = deviceRepository.findByBrand("NonExistent");

        assertThat(devices).isEmpty();
    }

    @Test
    @DisplayName("Should return empty list when no devices match state")
    void shouldReturnEmptyWhenNoStateMatch() {
        createDevice("iPhone 15", "Apple", DeviceState.AVAILABLE);

        List<Device> devices = deviceRepository.findByState(DeviceState.INACTIVE);

        assertThat(devices).isEmpty();
    }

    private Device createDevice(String name, String brand, DeviceState state) {
        Device device = new Device();
        device.setName(name);
        device.setBrand(brand);
        device.setState(state);
        return deviceRepository.save(device);
    }
}
