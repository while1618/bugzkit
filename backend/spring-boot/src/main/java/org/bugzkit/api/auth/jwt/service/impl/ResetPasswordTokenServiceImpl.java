package org.bugzkit.api.auth.jwt.service.impl;

import com.auth0.jwt.JWT;
import java.time.Instant;
import org.bugzkit.api.auth.jwt.event.OnSendJwtEmail;
import org.bugzkit.api.auth.jwt.redis.repository.UserBlacklistRepository;
import org.bugzkit.api.auth.jwt.service.ResetPasswordTokenService;
import org.bugzkit.api.auth.jwt.util.JwtUtil;
import org.bugzkit.api.auth.jwt.util.JwtUtil.JwtPurpose;
import org.bugzkit.api.shared.error.exception.BadRequestException;
import org.bugzkit.api.user.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class ResetPasswordTokenServiceImpl implements ResetPasswordTokenService {
  private static final JwtPurpose PURPOSE = JwtPurpose.RESET_PASSWORD_TOKEN;
  private final UserBlacklistRepository userBlacklistRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.reset-password-token.duration}")
  private int tokenDuration;

  public ResetPasswordTokenServiceImpl(
      UserBlacklistRepository userBlacklistRepository, ApplicationEventPublisher eventPublisher) {
    this.userBlacklistRepository = userBlacklistRepository;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public String create(Long userId) {
    return JWT.create()
        .withIssuer(userId.toString())
        .withClaim("purpose", PURPOSE.name())
        .withIssuedAt(Instant.now())
        .withExpiresAt(Instant.now().plusSeconds(tokenDuration))
        .sign(JwtUtil.getAlgorithm(secret));
  }

  @Override
  public void check(String token) {
    verifyToken(token);
    isInUserBlacklist(token);
  }

  private void verifyToken(String token) {
    try {
      JwtUtil.verify(token, secret, PURPOSE);
    } catch (RuntimeException e) {
      throw new BadRequestException("auth.tokenInvalid");
    }
  }

  private void isInUserBlacklist(String token) {
    final var userId = JwtUtil.getUserId(token);
    final var issuedAt = JwtUtil.getIssuedAt(token);
    final var userInBlacklist = userBlacklistRepository.findById(userId);
    if (userInBlacklist.isEmpty()) return;
    if (issuedAt.isBefore(userInBlacklist.get().getUpdatedAt()))
      throw new BadRequestException("auth.tokenInvalid");
  }

  @Override
  public void sendToEmail(User user, String token) {
    eventPublisher.publishEvent(new OnSendJwtEmail(user, token, PURPOSE));
  }
}
