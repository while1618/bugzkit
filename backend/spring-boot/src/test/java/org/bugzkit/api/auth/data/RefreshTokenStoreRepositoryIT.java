package org.bugzkit.api.auth.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.bugzkit.api.auth.jwt.redis.model.RefreshTokenStore;
import org.bugzkit.api.auth.jwt.redis.repository.RefreshTokenStoreRepository;
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
    final var first = new RefreshTokenStore("token1", 1L, "ip1", 1000);
    final var second = new RefreshTokenStore("token2", 2L, "ip2", 1000);
    final var third = new RefreshTokenStore("token3", 2L, "ip3", 1000);
    refreshTokenStoreRepository.saveAll(List.of(first, second, third));
  }

  @AfterEach
  void cleanUp() {
    refreshTokenStoreRepository.deleteAll();
  }

  @Test
  void findByUserIdAndIpAddress() {
    assertThat(refreshTokenStoreRepository.findByUserIdAndIpAddress(1L, "ip1")).isPresent();
  }

  @Test
  void findAllByUserId() {
    assertThat(refreshTokenStoreRepository.findAllByUserId(2L)).hasSize(2);
  }
}
