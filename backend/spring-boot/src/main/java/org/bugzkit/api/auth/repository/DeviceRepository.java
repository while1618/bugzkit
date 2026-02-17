package org.bugzkit.api.auth.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.bugzkit.api.auth.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<Device, Long> {
  List<Device> findAllByUserId(Long userId);

  Optional<Device> findByUserIdAndDeviceId(Long userId, String deviceId);

  void deleteByUserIdAndDeviceId(Long userId, String deviceId);

  void deleteAllByUserId(Long userId);

  void deleteByUserIdAndLastActiveAtBefore(Long userId, LocalDateTime cutoff);
}
