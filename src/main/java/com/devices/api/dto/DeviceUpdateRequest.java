package com.devices.api.dto;

import com.devices.api.enums.DeviceState;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request payload for updating a device. All fields are optional for partial updates.")
public record DeviceUpdateRequest(
        @Schema(description = "Device name", example = "iPhone 15 Pro Max")
        String name,

        @Schema(description = "Device brand", example = "Apple")
        String brand,

        @Schema(description = "Device state", example = "IN_USE")
        DeviceState state
) {
}
