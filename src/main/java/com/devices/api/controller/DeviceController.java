package com.devices.api.controller;

import com.devices.api.dto.DeviceRequest;
import com.devices.api.dto.DeviceResponse;
import com.devices.api.dto.DeviceUpdateRequest;
import com.devices.api.enums.DeviceState;
import com.devices.api.service.DeviceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping
    public ResponseEntity<DeviceResponse> createDevice(@Valid @RequestBody DeviceRequest request) {
        DeviceResponse response = deviceService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeviceResponse> getDevice(@PathVariable UUID id) {
        DeviceResponse response = deviceService.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<DeviceResponse>> getAllDevices(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) DeviceState state) {
        List<DeviceResponse> devices = deviceService.getAll(brand, state);
        return ResponseEntity.ok(devices);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeviceResponse> updateDevice(
            @PathVariable UUID id,
            @RequestBody DeviceUpdateRequest request) {
        DeviceResponse response = deviceService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<DeviceResponse> partialUpdateDevice(
            @PathVariable UUID id,
            @RequestBody DeviceUpdateRequest request) {
        DeviceResponse response = deviceService.partialUpdate(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable UUID id) {
        deviceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
