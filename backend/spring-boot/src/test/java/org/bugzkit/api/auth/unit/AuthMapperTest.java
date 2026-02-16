package org.bugzkit.api.auth.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.bugzkit.api.auth.mapper.AuthMapper;
import org.bugzkit.api.auth.model.Device;
import org.junit.jupiter.api.Test;

class AuthMapperTest {
  @Test
  void deviceToDeviceDTO_mapsAllFields() {
    final var now = LocalDateTime.now();
    final var device =
        Device.builder()
            .deviceId("device-1")
            .userAgent("Mozilla/5.0")
            .createdAt(now)
            .lastActiveAt(now)
            .build();

    final var dto = AuthMapper.INSTANCE.deviceToDeviceDTO(device, "device-1");

    assertEquals("device-1", dto.deviceId());
    assertEquals("Mozilla/5.0", dto.userAgent());
    assertEquals(now, dto.createdAt());
    assertEquals(now, dto.lastActiveAt());
  }

  @Test
  void deviceToDeviceDTO_currentTrue_whenDeviceIdMatches() {
    final var device = Device.builder().deviceId("device-1").build();

    final var dto = AuthMapper.INSTANCE.deviceToDeviceDTO(device, "device-1");

    assertTrue(dto.current());
  }

  @Test
  void deviceToDeviceDTO_currentFalse_whenDeviceIdDoesNotMatch() {
    final var device = Device.builder().deviceId("device-1").build();

    final var dto = AuthMapper.INSTANCE.deviceToDeviceDTO(device, "device-2");

    assertFalse(dto.current());
  }
}
