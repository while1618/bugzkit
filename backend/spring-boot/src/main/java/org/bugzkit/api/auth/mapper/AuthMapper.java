package org.bugzkit.api.auth.mapper;

import org.bugzkit.api.auth.model.Device;
import org.bugzkit.api.auth.payload.dto.DeviceDTO;
import org.mapstruct.Context;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface AuthMapper {
  AuthMapper INSTANCE = Mappers.getMapper(AuthMapper.class);

  @Mapping(target = "current", expression = "java(device.getDeviceId().equals(currentDeviceId))")
  DeviceDTO deviceToDeviceDTO(Device device, @Context String currentDeviceId);
}
