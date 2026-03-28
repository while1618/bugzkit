package org.bugzkit.api.auth.service;

import org.bugzkit.api.user.model.User;

public interface ResetPasswordTokenService {
  String create(Long userId);

  Long checkAndConsume(String token);

  void sendToEmail(User user, String token);
}
