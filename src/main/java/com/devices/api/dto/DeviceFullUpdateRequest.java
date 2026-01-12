package com.devices.api.dto;

import com.devices.api.enums.DeviceState;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request payload for fully updating a device. All fields are required.")
public record DeviceFullUpdateRequest(
        @Schema(description = "Device name", example = "iPhone 15 Pro")
        @NotBlank(message = "Name is required")
        String name,

        @Schema(description = "Device brand", example = "Apple")
        @NotBlank(message = "Brand is required")
        String brand,

        @Schema(description = "Device state", example = "AVAILABLE")
        @NotNull(message = "State is required")
        DeviceState state
) {
}
