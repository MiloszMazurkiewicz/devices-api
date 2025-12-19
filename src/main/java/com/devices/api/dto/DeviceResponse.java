package com.devices.api.dto;

import com.devices.api.enums.DeviceState;

import java.time.Instant;
import java.util.UUID;

public record DeviceResponse(
        UUID id,
        String name,
        String brand,
        DeviceState state,
        Instant creationTime
) {
}
