package org.bugzkit.api.auth.jwt.redis.repository;

import java.util.List;
import java.util.Optional;
import org.bugzkit.api.auth.jwt.redis.model.RefreshTokenStore;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenStoreRepository extends CrudRepository<RefreshTokenStore, String> {
  Optional<RefreshTokenStore> findByUserIdAndDeviceId(Long userId, String deviceId);

  List<RefreshTokenStore> findAllByUserId(Long userId);
}
