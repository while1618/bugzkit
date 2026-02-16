package org.bugzkit.api.auth.jwt.service;

import java.util.Optional;
import java.util.Set;
import org.bugzkit.api.user.payload.dto.RoleDTO;

public interface RefreshTokenService {
  String create(Long userId, Set<RoleDTO> roleDTOs, String deviceId);

  void check(String token);

  Optional<String> findByUserIdAndDeviceId(Long userId, String deviceId);

  void delete(String token);

  void deleteByUserIdAndDeviceId(Long userId, String deviceId);

  void deleteAllByUserId(Long userId);
}
