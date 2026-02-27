package org.bugzkit.api.auth.service.impl;

import com.auth0.jwt.JWT;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.bugzkit.api.auth.redis.model.AccessTokenBlacklist;
import org.bugzkit.api.auth.redis.model.UserBlacklist;
import org.bugzkit.api.auth.redis.repository.AccessTokenBlacklistRepository;
import org.bugzkit.api.auth.redis.repository.UserBlacklistRepository;
import org.bugzkit.api.auth.service.AccessTokenService;
import org.bugzkit.api.auth.util.JwtUtil;
import org.bugzkit.api.auth.util.JwtUtil.JwtPurpose;
import org.bugzkit.api.shared.error.exception.UnauthorizedException;
import org.bugzkit.api.user.payload.dto.RoleDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AccessTokenServiceImpl implements AccessTokenService {
  private static final JwtPurpose PURPOSE = JwtPurpose.ACCESS_TOKEN;
  private final AccessTokenBlacklistRepository accessTokenBlacklistRepository;
  private final UserBlacklistRepository userBlacklistRepository;

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.access-token.duration}")
  private int tokenDuration;

  public AccessTokenServiceImpl(
      AccessTokenBlacklistRepository accessTokenBlacklistRepository,
      UserBlacklistRepository userBlacklistRepository) {
    this.accessTokenBlacklistRepository = accessTokenBlacklistRepository;
    this.userBlacklistRepository = userBlacklistRepository;
  }

  @Override
  public String create(Long userId, Set<RoleDTO> roleDTOs, String deviceId) {
    return JWT.create()
        .withJWTId(UUID.randomUUID().toString())
        .withIssuer(userId.toString())
        .withClaim("roles", roleDTOs.stream().map(RoleDTO::name).toList())
        .withClaim("purpose", PURPOSE.name())
        .withClaim("deviceId", deviceId)
        .withIssuedAt(Instant.now())
        .withExpiresAt(Instant.now().plusSeconds(tokenDuration))
        .sign(JwtUtil.getAlgorithm(secret));
  }

  @Override
  public void check(String token) {
    verifyToken(token);
    isInAccessTokenBlacklist(token);
    isInUserBlacklist(token);
  }

  private void verifyToken(String token) {
    try {
      JwtUtil.verify(token, secret, PURPOSE);
    } catch (RuntimeException e) {
      throw new UnauthorizedException("auth.tokenInvalid");
    }
  }

  private void isInAccessTokenBlacklist(String token) {
    if (accessTokenBlacklistRepository.existsById(JwtUtil.getJwtId(token)))
      throw new UnauthorizedException("auth.tokenInvalid");
  }

  private void isInUserBlacklist(String token) {
    final var userId = JwtUtil.getUserId(token);
    final var issuedAt = JwtUtil.getIssuedAt(token);
    final var userInBlacklist = userBlacklistRepository.findById(userId);
    if (userInBlacklist.isEmpty()) return;
    if (issuedAt.isBefore(userInBlacklist.get().getUpdatedAt()))
      throw new UnauthorizedException("auth.tokenInvalid");
  }

  @Override
  public void invalidate(String token) {
    accessTokenBlacklistRepository.save(
        new AccessTokenBlacklist(JwtUtil.getJwtId(token), tokenDuration));
  }

  @Override
  public void invalidateAllByUserId(Long userId) {
    userBlacklistRepository.save(
        UserBlacklist.builder().userId(userId).timeToLive(tokenDuration).build());
  }
}
