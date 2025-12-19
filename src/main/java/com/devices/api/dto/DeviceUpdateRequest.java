package com.devices.api.dto;

import com.devices.api.enums.DeviceState;

public record DeviceUpdateRequest(
        String name,
        String brand,
        DeviceState state
) {
}
