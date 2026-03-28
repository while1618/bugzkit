package org.bugzkit.api.auth.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bugzkit.api.shared.config.TestcontainersConfig;
import org.bugzkit.api.user.model.User;
import org.bugzkit.api.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@ActiveProfiles("test")
@SpringBootTest
class OAuth2UserServiceIT extends TestcontainersConfig {
  @Autowired private UserRepository userRepository;

  @Test
  void oAuthUser_createdWithNullUsernameAndPassword() {
    final var user =
        userRepository.save(
            User.builder().email("oauth-new@gmail.com").active(true).lock(false).build());

    assertNotNull(user.getId());
    assertEquals("oauth-new@gmail.com", user.getEmail());
    assertNull(user.getUsername());
    assertNull(user.getPassword());
    assertTrue(user.getActive());
  }

  @Test
  void oAuthUser_existingUserFoundByEmail() {
    final var email = "user@localhost";
    final var countBefore = userRepository.count();

    final var found = userRepository.findWithRolesByEmail(email);

    assertTrue(found.isPresent());
    assertEquals("user", found.get().getUsername());
    assertEquals(countBefore, userRepository.count());
  }

  @Test
  void oAuthUser_newUserFoundByEmailAfterCreation() {
    final var email = "oauth-find@gmail.com";
    assertTrue(userRepository.findByEmail(email).isEmpty());

    userRepository.save(User.builder().email(email).active(true).lock(false).build());

    final var found = userRepository.findWithRolesByEmail(email);
    assertTrue(found.isPresent());
    assertNull(found.get().getUsername());
  }
}
