package org.bootstrapbugz.api.auth.jwt.service;

import java.util.Set;
import org.bootstrapbugz.api.user.model.Role;

public interface RefreshTokenService {
  String create(Long userId, Set<Role> roles, String ipAddress);

  void check(String token);

  String findByUserAndIpAddress(Long userId, String ipAddress);

  void delete(String token);

  void deleteByUserAndIpAddress(Long userId, String ipAddress);

  void deleteAllByUser(Long userId);
}