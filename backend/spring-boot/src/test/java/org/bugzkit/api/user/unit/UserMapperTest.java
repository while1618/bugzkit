package org.bugzkit.api.user.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.util.Set;
import org.bugzkit.api.user.mapper.UserMapper;
import org.bugzkit.api.user.model.Role;
import org.bugzkit.api.user.model.Role.RoleName;
import org.bugzkit.api.user.model.User;
import org.junit.jupiter.api.Test;

class UserMapperTest {
  @Test
  void userToAdminUserDTO_mapsAllFields() {
    final var now = LocalDateTime.now();
    final var role = new Role(RoleName.USER);
    final var user =
        User.builder()
            .id(1L)
            .username("admin")
            .email("admin@localhost")
            .active(true)
            .lock(false)
            .createdAt(now)
            .roles(Set.of(role))
            .build();

    final var dto = UserMapper.INSTANCE.userToAdminUserDTO(user);

    assertEquals(1L, dto.id());
    assertEquals("admin", dto.username());
    assertEquals("admin@localhost", dto.email());
    assertEquals(true, dto.active());
    assertEquals(false, dto.lock());
    assertEquals(now, dto.createdAt());
    assertNotNull(dto.roleDTOs());
    assertEquals(1, dto.roleDTOs().size());
    assertEquals("USER", dto.roleDTOs().iterator().next().name());
  }

  @Test
  void userToProfileUserDTO_ignoresActiveLockAndRoles() {
    final var user =
        User.builder()
            .id(1L)
            .username("user")
            .email("user@localhost")
            .active(true)
            .lock(false)
            .build();

    final var dto = UserMapper.INSTANCE.userToProfileUserDTO(user);

    assertEquals("user", dto.username());
    assertEquals("user@localhost", dto.email());
    assertNull(dto.active());
    assertNull(dto.lock());
    assertNull(dto.roleDTOs());
  }

  @Test
  void userToSimpleUserDTO_ignoresEmailActiveLockAndRoles() {
    final var user =
        User.builder()
            .id(1L)
            .username("user")
            .email("user@localhost")
            .active(true)
            .lock(false)
            .build();

    final var dto = UserMapper.INSTANCE.userToSimpleUserDTO(user);

    assertEquals("user", dto.username());
    assertNull(dto.email());
    assertNull(dto.active());
    assertNull(dto.lock());
    assertNull(dto.roleDTOs());
  }
}
