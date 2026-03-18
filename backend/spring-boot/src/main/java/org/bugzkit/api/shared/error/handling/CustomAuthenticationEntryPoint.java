package org.bugzkit.api.shared.error.handling;

import com.google.common.net.HttpHeaders;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.bugzkit.api.shared.error.ErrorMessage;
import org.bugzkit.api.shared.message.service.MessageService;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
  private final MessageService messageService;

  public CustomAuthenticationEntryPoint(MessageService messageService) {
    this.messageService = messageService;
  }

  @Override
  public void commence(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException e)
      throws IOException {
    final var requestId = UUID.randomUUID().toString();
    MDC.put("REQUEST_ID", requestId);
    MDC.put("REQUEST_METHOD", request.getMethod());
    MDC.put("REQUEST_URL", request.getRequestURL().toString());
    final var errorMessage = new ErrorMessage(HttpStatus.UNAUTHORIZED);
    errorMessage.addCode(messageService.getMessage("auth.unauthorized"));
    response.setHeader(HttpHeaders.X_REQUEST_ID, requestId);
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.getOutputStream().println(errorMessage.toString());
    log.error("Auth failed", e);
    MDC.clear();
  }
}
