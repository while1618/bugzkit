package org.bugzkit.api.auth.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import org.bugzkit.api.auth.jwt.service.AccessTokenService;
import org.bugzkit.api.auth.jwt.service.RefreshTokenService;
import org.bugzkit.api.auth.mapper.AuthMapper;
import org.bugzkit.api.auth.model.Device;
import org.bugzkit.api.auth.payload.dto.DeviceDTO;
import org.bugzkit.api.auth.repository.DeviceRepository;
import org.bugzkit.api.auth.service.DeviceService;
import org.bugzkit.api.auth.util.AuthUtil;
import org.bugzkit.api.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeviceServiceImpl implements DeviceService {
  private final DeviceRepository deviceRepository;
  private final UserRepository userRepository;
  private final AccessTokenService accessTokenService;
  private final RefreshTokenService refreshTokenService;

  public DeviceServiceImpl(
      DeviceRepository deviceRepository,
      UserRepository userRepository,
      AccessTokenService accessTokenService,
      RefreshTokenService refreshTokenService) {
    this.deviceRepository = deviceRepository;
    this.userRepository = userRepository;
    this.accessTokenService = accessTokenService;
    this.refreshTokenService = refreshTokenService;
  }

  @Override
  public List<DeviceDTO> findAll(String currentDeviceId) {
    final var userId = AuthUtil.findSignedInUser().getId();
    return deviceRepository.findAllByUserId(userId).stream()
        .map(device -> AuthMapper.INSTANCE.deviceToDeviceDTO(device, currentDeviceId))
        .toList();
  }

  @Override
  @Transactional
  public void createOrUpdate(Long userId, String deviceId, String userAgent) {
    final var existing = deviceRepository.findByUserIdAndDeviceId(userId, deviceId);
    if (existing.isPresent()) {
      final var device = existing.get();
      device.setUserAgent(userAgent);
      device.setLastActiveAt(LocalDateTime.now());
      deviceRepository.save(device);
    } else {
      deviceRepository.save(
          Device.builder()
              .user(userRepository.getReferenceById(userId))
              .deviceId(deviceId)
              .userAgent(userAgent)
              .build());
    }
  }

  @Override
  @Transactional
  public void revoke(String deviceId) {
    final var userId = AuthUtil.findSignedInUser().getId();
    deviceRepository.deleteByUserIdAndDeviceId(userId, deviceId);
    refreshTokenService.deleteByUserIdAndDeviceId(userId, deviceId);
    accessTokenService.invalidateAllByUserId(userId);
  }

  @Override
  @Transactional
  public void deleteByUserIdAndDeviceId(Long userId, String deviceId) {
    deviceRepository.deleteByUserIdAndDeviceId(userId, deviceId);
  }

  @Override
  @Transactional
  public void deleteAllByUserId(Long userId) {
    deviceRepository.deleteAllByUserId(userId);
  }
}
