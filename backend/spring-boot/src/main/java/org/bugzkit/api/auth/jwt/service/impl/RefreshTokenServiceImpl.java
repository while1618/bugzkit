package org.bugzkit.api.auth.jwt.service.impl;

import com.auth0.jwt.JWT;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import org.bugzkit.api.auth.jwt.redis.model.RefreshTokenStore;
import org.bugzkit.api.auth.jwt.redis.repository.RefreshTokenStoreRepository;
import org.bugzkit.api.auth.jwt.service.RefreshTokenService;
import org.bugzkit.api.auth.jwt.util.JwtUtil;
import org.bugzkit.api.auth.jwt.util.JwtUtil.JwtPurpose;
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
    final var token =
        JWT.create()
            .withIssuer(userId.toString())
            .withClaim("roles", roleDTOs.stream().map(RoleDTO::name).toList())
            .withClaim("purpose", PURPOSE.name())
            .withIssuedAt(Instant.now())
            .withExpiresAt(Instant.now().plusSeconds(tokenDuration))
            .sign(JwtUtil.getAlgorithm(secret));
    refreshTokenStoreRepository.save(new RefreshTokenStore(token, userId, deviceId, tokenDuration));
    return token;
  }

  @Override
  public void check(String token) {
    verifyToken(token);
    isInRefreshTokenStore(token);
  }

  private void verifyToken(String token) {
    try {
      JwtUtil.verify(token, secret, PURPOSE);
    } catch (RuntimeException e) {
      throw new BadRequestException("auth.tokenInvalid");
    }
  }

  private void isInRefreshTokenStore(String token) {
    if (!refreshTokenStoreRepository.existsById(token))
      throw new BadRequestException("auth.tokenInvalid");
  }

  @Override
  public Optional<String> findByUserIdAndDeviceId(Long userId, String deviceId) {
    return refreshTokenStoreRepository
        .findByUserIdAndDeviceId(userId, deviceId)
        .map(RefreshTokenStore::getRefreshToken);
  }

  @Override
  public void delete(String token) {
    refreshTokenStoreRepository.deleteById(token);
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
