package org.bugzkit.api.auth.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.bugzkit.api.shared.logger.CustomLogger;
import org.bugzkit.api.shared.message.service.MessageService;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2FailureHandler implements AuthenticationFailureHandler {
  private final MessageService messageService;
  private final CustomLogger customLogger;

  @Value("${ui.url}")
  private String uiUrl;

  public OAuth2FailureHandler(MessageService messageService, CustomLogger customLogger) {
    this.messageService = messageService;
    this.customLogger = customLogger;
  }

  @Override
  public void onAuthenticationFailure(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
      throws IOException {
    final var errorCode = getCode(exception);
    response.sendRedirect(
        uiUrl + "/auth/sign-in?error=" + URLEncoder.encode(errorCode, StandardCharsets.UTF_8));
    customLogger.error("OAuth failed", exception);
    MDC.remove("REQUEST_ID");
  }

  private String getCode(AuthenticationException exception) {
    try {
      return messageService.getMessage(exception.getMessage());
    } catch (Exception e) {
      return messageService.getMessage("server.internalError");
    }
  }
}
