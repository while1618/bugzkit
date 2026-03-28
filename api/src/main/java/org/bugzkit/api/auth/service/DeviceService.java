package org.bugzkit.api.auth.service;

import java.util.List;
import org.bugzkit.api.auth.payload.dto.DeviceDTO;

public interface DeviceService {
  List<DeviceDTO> findAll(String currentDeviceId);

  void createOrUpdate(Long userId, String deviceId, String userAgent);

  void revoke(String deviceId);

  void deleteByUserIdAndDeviceId(Long userId, String deviceId);

  void deleteAllByUserId(Long userId);
}
