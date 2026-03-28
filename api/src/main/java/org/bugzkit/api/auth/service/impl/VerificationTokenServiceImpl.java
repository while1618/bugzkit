package org.bugzkit.api.auth.service.impl;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.bugzkit.api.auth.email.AuthEmailPurpose;
import org.bugzkit.api.auth.email.OnSendAuthEmail;
import org.bugzkit.api.auth.redis.model.VerificationTokenStore;
import org.bugzkit.api.auth.redis.repository.VerificationTokenStoreRepository;
import org.bugzkit.api.auth.service.VerificationTokenService;
import org.bugzkit.api.shared.error.exception.BadRequestException;
import org.bugzkit.api.user.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class VerificationTokenServiceImpl implements VerificationTokenService {
  private final VerificationTokenStoreRepository verificationTokenStoreRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Value("${jwt.verify-email-token.duration}")
  private int tokenDuration;

  public VerificationTokenServiceImpl(
      VerificationTokenStoreRepository verificationTokenStoreRepository,
      ApplicationEventPublisher eventPublisher) {
    this.verificationTokenStoreRepository = verificationTokenStoreRepository;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public String create(Long userId) {
    final var token = UUID.randomUUID().toString();
    verificationTokenStoreRepository.save(new VerificationTokenStore(token, userId, tokenDuration));
    return token;
  }

  @Override
  public Long checkAndConsume(String token) {
    final var stored =
        verificationTokenStoreRepository
            .findById(token)
            .orElseThrow(
                () -> {
                  log.warn(
                      "Verification token '{}' not found in store (already consumed or expired)",
                      token);
                  return new BadRequestException("auth.tokenInvalid");
                });
    verificationTokenStoreRepository.deleteById(token);
    return stored.getUserId();
  }

  @Override
  public void sendToEmail(User user, String token) {
    eventPublisher.publishEvent(new OnSendAuthEmail(user, token, AuthEmailPurpose.VERIFY_EMAIL));
  }
}
