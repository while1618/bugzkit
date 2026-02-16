package org.bugzkit.api.auth.payload.dto;

import java.time.LocalDateTime;

public record DeviceDTO(
    String deviceId,
    String userAgent,
    LocalDateTime createdAt,
    LocalDateTime lastActiveAt,
    boolean current) {}
