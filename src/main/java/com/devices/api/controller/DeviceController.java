package com.devices.api.controller;

import com.devices.api.dto.DeviceRequest;
import com.devices.api.dto.DeviceResponse;
import com.devices.api.dto.DeviceUpdateRequest;
import com.devices.api.enums.DeviceState;
import com.devices.api.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/devices")
@Tag(name = "Devices", description = "Device management operations")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Operation(summary = "Create a new device", description = "Creates a new device with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Device created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<DeviceResponse> createDevice(@Valid @RequestBody DeviceRequest request) {
        DeviceResponse response = deviceService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get device by ID", description = "Retrieves a device by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Device found"),
            @ApiResponse(responseCode = "404", description = "Device not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<DeviceResponse> getDevice(
            @Parameter(description = "Device ID") @PathVariable UUID id) {
        DeviceResponse response = deviceService.getById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all devices", description = "Retrieves all devices with optional filtering by brand and/or state")
    @ApiResponse(responseCode = "200", description = "List of devices retrieved successfully")
    @GetMapping
    public ResponseEntity<List<DeviceResponse>> getAllDevices(
            @Parameter(description = "Filter by brand") @RequestParam(required = false) String brand,
            @Parameter(description = "Filter by state") @RequestParam(required = false) DeviceState state) {
        List<DeviceResponse> devices = deviceService.getAll(brand, state);
        return ResponseEntity.ok(devices);
    }

    @Operation(summary = "Update device", description = "Fully updates an existing device. Name and brand cannot be updated if device is in use.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Device updated successfully"),
            @ApiResponse(responseCode = "404", description = "Device not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Device is in use and cannot be modified",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<DeviceResponse> updateDevice(
            @Parameter(description = "Device ID") @PathVariable UUID id,
            @RequestBody DeviceUpdateRequest request) {
        DeviceResponse response = deviceService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Partially update device", description = "Partially updates an existing device. Name and brand cannot be updated if device is in use.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Device updated successfully"),
            @ApiResponse(responseCode = "404", description = "Device not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Device is in use and cannot be modified",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PatchMapping("/{id}")
    public ResponseEntity<DeviceResponse> partialUpdateDevice(
            @Parameter(description = "Device ID") @PathVariable UUID id,
            @RequestBody DeviceUpdateRequest request) {
        DeviceResponse response = deviceService.partialUpdate(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete device", description = "Deletes a device. Devices that are in use cannot be deleted.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Device deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Device not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Device is in use and cannot be deleted",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(
            @Parameter(description = "Device ID") @PathVariable UUID id) {
        deviceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
