package org.bugzkit.api.auth.service;

import java.util.Set;
import org.bugzkit.api.user.payload.dto.RoleDTO;

public interface AccessTokenService {
  String create(Long userId, Set<RoleDTO> roleDTOs, String deviceId);

  void check(String token);

  void invalidate(String token);

  void invalidateAllByUserId(Long userId);
}
