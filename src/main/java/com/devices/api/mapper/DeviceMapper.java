package com.devices.api.mapper;

import com.devices.api.dto.DeviceRequest;
import com.devices.api.dto.DeviceResponse;
import com.devices.api.entity.Device;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DeviceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationTime", ignore = true)
    Device toEntity(DeviceRequest request);

    DeviceResponse toResponse(Device device);

    List<DeviceResponse> toResponseList(List<Device> devices);
}
