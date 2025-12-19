package com.devices.api.dto;

import com.devices.api.enums.DeviceState;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Device response payload")
public record DeviceResponse(
        @Schema(description = "Unique device identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,

        @Schema(description = "Device name", example = "iPhone 15 Pro")
        String name,

        @Schema(description = "Device brand", example = "Apple")
        String brand,

        @Schema(description = "Device state", example = "AVAILABLE")
        DeviceState state,

        @Schema(description = "Device creation timestamp", example = "2024-01-15T10:30:00Z")
        Instant creationTime
) {
}
