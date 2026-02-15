package org.bugzkit.api.user.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.bugzkit.api.shared.config.DatabaseContainers;
import org.bugzkit.api.user.model.Role;
import org.bugzkit.api.user.model.Role.RoleName;
import org.bugzkit.api.user.model.User;
import org.bugzkit.api.user.repository.RoleRepository;
import org.bugzkit.api.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;

@DataJpaTest
@DirtiesContext
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryIT extends DatabaseContainers {
  @Autowired private UserRepository userRepository;
  @Autowired private RoleRepository roleRepository;

  @BeforeEach
  void setUp() {
    final var adminRole = roleRepository.save(new Role(RoleName.ADMIN));
    final var userRole = roleRepository.save(new Role(RoleName.USER));
    userRepository.save(
        User.builder()
            .username("admin")
            .email("admin@localhost")
            .password("password")
            .roles(Set.of(adminRole, userRole))
            .build());
    userRepository.save(
        User.builder()
            .username("user")
            .email("user@localhost")
            .password("password")
            .roles(Set.of(userRole))
            .build());
  }

  @AfterEach
  void cleanUp() {
    userRepository.deleteAll();
    roleRepository.deleteAll();
  }

  @Test
  void findAllByIdIn() {
    final var userIds = userRepository.findAllUserIds(PageRequest.of(0, 10));
    assertThat(userIds).hasSize(2);
    final var users = userRepository.findAllByIdIn(userIds);
    assertThat(users.getFirst().getUsername()).isEqualTo("admin");
    assertThat(users).hasSize(2);
  }

  @Test
  void findByIdWithRoles() {
    final var id = userRepository.findByUsername("admin").orElseThrow().getId();
    final var user = userRepository.findWithRolesById(id).orElseThrow();
    assertThat(user.getUsername()).isEqualTo("admin");
    assertThat(user.getRoles()).hasSize(2);
  }

  @Test
  void findByUsername() {
    assertThat(userRepository.findByUsername("admin")).isPresent();
  }

  @Test
  void findByUsernameWithRoles() {
    final var user = userRepository.findWithRolesByUsername("user").orElseThrow();
    assertThat(user.getUsername()).isEqualTo("user");
    assertThat(user.getRoles()).hasSize(1);
  }

  @Test
  void findByEmailWithRoles() {
    final var user = userRepository.findWithRolesByEmail("user@localhost").orElseThrow();
    assertThat(user.getUsername()).isEqualTo("user");
    assertThat(user.getRoles()).hasSize(1);
  }

  @Test
  void findByEmail() {
    assertThat(userRepository.findByEmail("admin@localhost")).isPresent();
  }

  @Test
  void findByUsernameOrEmail() {
    assertThat(userRepository.findByUsernameOrEmail("admin", "admin@localhost")).isPresent();
  }

  @Test
  void existsByUsername() {
    assertThat(userRepository.existsByUsername("admin")).isTrue();
  }

  @Test
  void existsByEmail() {
    assertThat(userRepository.existsByEmail("admin@localhost")).isTrue();
  }
}
