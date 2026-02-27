package org.bugzkit.api.auth.service.impl;

import com.auth0.jwt.JWT;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.bugzkit.api.auth.redis.model.RefreshTokenStore;
import org.bugzkit.api.auth.redis.repository.RefreshTokenStoreRepository;
import org.bugzkit.api.auth.service.RefreshTokenService;
import org.bugzkit.api.auth.util.JwtUtil;
import org.bugzkit.api.auth.util.JwtUtil.JwtPurpose;
import org.bugzkit.api.shared.error.exception.BadRequestException;
import org.bugzkit.api.user.payload.dto.RoleDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {
  private static final JwtPurpose PURPOSE = JwtPurpose.REFRESH_TOKEN;
  private final RefreshTokenStoreRepository refreshTokenStoreRepository;

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.refresh-token.duration}")
  private int tokenDuration;

  public RefreshTokenServiceImpl(RefreshTokenStoreRepository refreshTokenStoreRepository) {
    this.refreshTokenStoreRepository = refreshTokenStoreRepository;
  }

  @Override
  public String create(Long userId, Set<RoleDTO> roleDTOs, String deviceId) {
    final var jti = UUID.randomUUID().toString();
    final var token =
        JWT.create()
            .withJWTId(jti)
            .withIssuer(userId.toString())
            .withClaim("roles", roleDTOs.stream().map(RoleDTO::name).toList())
            .withClaim("purpose", PURPOSE.name())
            .withClaim("deviceId", deviceId)
            .withIssuedAt(Instant.now())
            .withExpiresAt(Instant.now().plusSeconds(tokenDuration))
            .sign(JwtUtil.getAlgorithm(secret));
    refreshTokenStoreRepository.save(new RefreshTokenStore(jti, userId, deviceId, tokenDuration));
    return token;
  }

  @Override
  public void checkAndConsume(String token) {
    verifyToken(token);
    final var jti = JwtUtil.getJwtId(token);
    if (!refreshTokenStoreRepository.existsById(jti))
      throw new BadRequestException("auth.tokenInvalid");
    refreshTokenStoreRepository.deleteById(jti);
  }

  private void verifyToken(String token) {
    try {
      JwtUtil.verify(token, secret, PURPOSE);
    } catch (RuntimeException e) {
      throw new BadRequestException("auth.tokenInvalid");
    }
  }

  @Override
  public void deleteByUserIdAndDeviceId(Long userId, String deviceId) {
    refreshTokenStoreRepository
        .findByUserIdAndDeviceId(userId, deviceId)
        .ifPresent(refreshTokenStoreRepository::delete);
  }

  @Override
  public void deleteAllByUserId(Long userId) {
    final var refreshTokens = refreshTokenStoreRepository.findAllByUserId(userId);
    refreshTokenStoreRepository.deleteAll(refreshTokens);
  }
}
