package org.bugzkit.api.auth.jwt.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;
import org.bugzkit.api.user.payload.dto.RoleDTO;

public class JwtUtil {
  private JwtUtil() {}

  public static void verify(String token, String secret, JwtPurpose purpose) {
    JWT.require(JwtUtil.getAlgorithm(secret))
        .withClaim("purpose", purpose.name())
        .build()
        .verify(token);
  }

  public static Algorithm getAlgorithm(String secret) {
    return Algorithm.HMAC512(secret.getBytes());
  }

  public static Long getUserId(String token) {
    return Long.parseLong(JWT.decode(token).getIssuer());
  }

  public static Set<RoleDTO> getRoleDTOs(String token) {
    final var roles = JWT.decode(token).getClaim("roles").asList(String.class);
    return roles.stream().map(RoleDTO::new).collect(Collectors.toSet());
  }

  public static String getDeviceId(String token) {
    return JWT.decode(token).getClaim("deviceId").asString();
  }

  public static Instant getIssuedAt(String token) {
    return JWT.decode(token).getIssuedAtAsInstant();
  }

  public enum JwtPurpose {
    ACCESS_TOKEN,
    REFRESH_TOKEN,
    VERIFY_EMAIL_TOKEN,
    RESET_PASSWORD_TOKEN
  }
}
