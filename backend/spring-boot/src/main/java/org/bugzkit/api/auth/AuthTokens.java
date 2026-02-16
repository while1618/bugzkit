package org.bugzkit.api.auth;

public record AuthTokens(String accessToken, String refreshToken, String deviceId) {}
