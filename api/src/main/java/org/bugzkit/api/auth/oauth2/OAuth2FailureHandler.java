package org.bugzkit.api.auth.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.bugzkit.api.shared.message.service.MessageService;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OAuth2FailureHandler implements AuthenticationFailureHandler {
  private final MessageService messageService;

  @Value("${ui.url}")
  private String uiUrl;

  public OAuth2FailureHandler(MessageService messageService) {
    this.messageService = messageService;
  }

  @Override
  public void onAuthenticationFailure(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
      throws IOException {
    final var errorCode = getCode(exception);
    response.sendRedirect(
        uiUrl + "/auth/sign-in?error=" + URLEncoder.encode(errorCode, StandardCharsets.UTF_8));
    log.error("OAuth failed", exception);
    MDC.clear();
  }

  private String getCode(AuthenticationException exception) {
    try {
      return messageService.getMessage(exception.getMessage());
    } catch (Exception e) {
      return messageService.getMessage("server.internalError");
    }
  }
}
