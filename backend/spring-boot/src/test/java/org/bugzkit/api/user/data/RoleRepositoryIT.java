package org.bugzkit.api.user.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.bugzkit.api.shared.config.DatabaseContainers;
import org.bugzkit.api.user.model.Role;
import org.bugzkit.api.user.model.Role.RoleName;
import org.bugzkit.api.user.repository.RoleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.annotation.DirtiesContext;

@DataJpaTest
@DirtiesContext
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RoleRepositoryIT extends DatabaseContainers {
  @Autowired private RoleRepository roleRepository;

  @BeforeEach
  void setUp() {
    roleRepository.save(new Role(RoleName.ADMIN));
    roleRepository.save(new Role(RoleName.USER));
  }

  @AfterEach
  void cleanUp() {
    roleRepository.deleteAll();
  }

  @Test
  void findAllByNameIn() {
    final var roles = roleRepository.findAllByNameIn(Set.of(RoleName.USER, RoleName.ADMIN));
    assertThat(roles).hasSize(2);
  }

  @Test
  void findByName() {
    assertThat(roleRepository.findByName(RoleName.USER)).isPresent();
  }
}
