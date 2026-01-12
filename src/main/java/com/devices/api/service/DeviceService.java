package com.devices.api.service;

import com.devices.api.dto.DeviceFullUpdateRequest;
import com.devices.api.dto.DeviceRequest;
import com.devices.api.dto.DeviceResponse;
import com.devices.api.dto.DeviceUpdateRequest;
import com.devices.api.enums.DeviceState;

import java.util.List;
import java.util.UUID;

public interface DeviceService {

    DeviceResponse create(DeviceRequest request);

    DeviceResponse getById(UUID id);

    List<DeviceResponse> getAll(String brand, DeviceState state);

    DeviceResponse update(UUID id, DeviceFullUpdateRequest request);

    DeviceResponse partialUpdate(UUID id, DeviceUpdateRequest request);

    void delete(UUID id);
}
