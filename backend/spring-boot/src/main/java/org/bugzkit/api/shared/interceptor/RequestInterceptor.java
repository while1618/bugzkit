package org.bugzkit.api.shared.interceptor;

import com.google.common.net.HttpHeaders;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.bugzkit.api.auth.util.AuthUtil;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class RequestInterceptor implements HandlerInterceptor {
  @Override
  public boolean preHandle(
      @Nonnull HttpServletRequest request,
      @Nonnull HttpServletResponse response,
      @Nonnull Object handler) {
    final var requestId = UUID.randomUUID().toString();
    response.setHeader(HttpHeaders.X_REQUEST_ID, requestId);
    MDC.put("REQUEST_ID", requestId);
    MDC.put("USER", AuthUtil.getAuthName());
    MDC.put("REQUEST_METHOD", request.getMethod());
    MDC.put("REQUEST_URL", request.getRequestURL().toString());
    final var method = (HandlerMethod) handler;
    log.info("→ {}.{}", method.getBeanType().getSimpleName(), method.getMethod().getName());
    return true;
  }

  @Override
  public void afterCompletion(
      @Nonnull HttpServletRequest request,
      @Nonnull HttpServletResponse response,
      @Nonnull Object handler,
      Exception ex) {
    final var method = (HandlerMethod) handler;
    log.info(
        "← {}.{} [{}]",
        method.getBeanType().getSimpleName(),
        method.getMethod().getName(),
        response.getStatus());
    MDC.clear();
  }
}
