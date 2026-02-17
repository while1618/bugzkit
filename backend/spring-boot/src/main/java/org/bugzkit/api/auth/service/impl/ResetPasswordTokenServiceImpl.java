package org.bugzkit.api.auth.service.impl;

import java.util.UUID;
import org.bugzkit.api.auth.email.AuthEmailPurpose;
import org.bugzkit.api.auth.email.OnSendAuthEmail;
import org.bugzkit.api.auth.redis.model.ResetPasswordTokenStore;
import org.bugzkit.api.auth.redis.repository.ResetPasswordTokenStoreRepository;
import org.bugzkit.api.auth.service.ResetPasswordTokenService;
import org.bugzkit.api.shared.error.exception.BadRequestException;
import org.bugzkit.api.user.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class ResetPasswordTokenServiceImpl implements ResetPasswordTokenService {
  private final ResetPasswordTokenStoreRepository resetPasswordTokenStoreRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Value("${jwt.reset-password-token.duration}")
  private int tokenDuration;

  public ResetPasswordTokenServiceImpl(
      ResetPasswordTokenStoreRepository resetPasswordTokenStoreRepository,
      ApplicationEventPublisher eventPublisher) {
    this.resetPasswordTokenStoreRepository = resetPasswordTokenStoreRepository;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public String create(Long userId) {
    final var token = UUID.randomUUID().toString();
    resetPasswordTokenStoreRepository.save(
        new ResetPasswordTokenStore(token, userId, tokenDuration));
    return token;
  }

  @Override
  public Long checkAndConsume(String token) {
    final var stored =
        resetPasswordTokenStoreRepository
            .findById(token)
            .orElseThrow(() -> new BadRequestException("auth.tokenInvalid"));
    resetPasswordTokenStoreRepository.deleteById(token);
    return stored.getUserId();
  }

  @Override
  public void sendToEmail(User user, String token) {
    eventPublisher.publishEvent(new OnSendAuthEmail(user, token, AuthEmailPurpose.RESET_PASSWORD));
  }
}
