package org.bugzkit.api.auth.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.bugzkit.api.auth.redis.model.RefreshTokenStore;
import org.bugzkit.api.auth.redis.repository.RefreshTokenStoreRepository;
import org.bugzkit.api.shared.config.DatabaseContainers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.redis.test.autoconfigure.DataRedisTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.annotation.DirtiesContext;

@DataRedisTest
@DirtiesContext
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RefreshTokenStoreRepositoryIT extends DatabaseContainers {
  @Autowired private RefreshTokenStoreRepository refreshTokenStoreRepository;

  @BeforeEach
  void setUp() {
    refreshTokenStoreRepository.deleteAll();
    final var first = new RefreshTokenStore("token1", 1L, "device1", 1000);
    final var second = new RefreshTokenStore("token2", 2L, "device2", 1000);
    final var third = new RefreshTokenStore("token3", 2L, "device3", 1000);
    refreshTokenStoreRepository.saveAll(List.of(first, second, third));
  }

  @AfterEach
  void cleanUp() {
    refreshTokenStoreRepository.deleteAll();
  }

  @Test
  void findByUserIdAndDeviceId() {
    assertThat(refreshTokenStoreRepository.findByUserIdAndDeviceId(1L, "device1")).isPresent();
  }

  @Test
  void findAllByUserId() {
    assertThat(refreshTokenStoreRepository.findAllByUserId(2L)).hasSize(2);
  }
}
