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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceMapper deviceMapper;

    public DeviceServiceImpl(DeviceRepository deviceRepository, DeviceMapper deviceMapper) {
        this.deviceRepository = deviceRepository;
        this.deviceMapper = deviceMapper;
    }

    @Override
    public DeviceResponse create(DeviceRequest request) {
        Device device = deviceMapper.toEntity(request);
        Device savedDevice = deviceRepository.save(device);
        return deviceMapper.toResponse(savedDevice);
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceResponse getById(UUID id) {
        Device device = findDeviceOrThrow(id);
        return deviceMapper.toResponse(device);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceResponse> getAll(String brand, DeviceState state) {
        List<Device> devices;

        if (brand != null && state != null) {
            devices = deviceRepository.findByBrandAndState(brand, state);
        } else if (brand != null) {
            devices = deviceRepository.findByBrand(brand);
        } else if (state != null) {
            devices = deviceRepository.findByState(state);
        } else {
            devices = deviceRepository.findAll();
        }

        return deviceMapper.toResponseList(devices);
    }

    @Override
    public DeviceResponse update(UUID id, DeviceUpdateRequest request) {
        Device device = findDeviceOrThrow(id);

        validateUpdateAllowed(device, request);

        if (request.name() != null) {
            device.setName(request.name());
        }
        if (request.brand() != null) {
            device.setBrand(request.brand());
        }
        if (request.state() != null) {
            device.setState(request.state());
        }

        Device updatedDevice = deviceRepository.save(device);
        return deviceMapper.toResponse(updatedDevice);
    }

    @Override
    public DeviceResponse partialUpdate(UUID id, DeviceUpdateRequest request) {
        return update(id, request);
    }

    @Override
    public void delete(UUID id) {
        Device device = findDeviceOrThrow(id);

        if (device.getState() == DeviceState.IN_USE) {
            throw new DeviceInUseException("Cannot delete device that is in use");
        }

        deviceRepository.delete(device);
    }

    private Device findDeviceOrThrow(UUID id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException(id));
    }

    private void validateUpdateAllowed(Device device, DeviceUpdateRequest request) {
        if (device.getState() == DeviceState.IN_USE) {
            if (request.name() != null || request.brand() != null) {
                throw new DeviceInUseException(
                        "Cannot update name or brand of device that is in use");
            }
        }
    }
}
