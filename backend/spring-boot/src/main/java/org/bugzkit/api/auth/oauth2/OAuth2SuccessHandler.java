package org.bugzkit.api.auth.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;
import org.bugzkit.api.auth.jwt.service.AccessTokenService;
import org.bugzkit.api.auth.jwt.service.RefreshTokenService;
import org.bugzkit.api.auth.service.DeviceService;
import org.bugzkit.api.auth.util.AuthUtil;
import org.bugzkit.api.shared.logger.CustomLogger;
import org.bugzkit.api.user.payload.dto.RoleDTO;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
  private final AccessTokenService accessTokenService;
  private final RefreshTokenService refreshTokenService;
  private final DeviceService deviceService;
  private final CustomLogger customLogger;

  @Value("${domain.name}")
  private String domain;

  @Value("${ui.url}")
  private String uiUrl;

  @Value("${jwt.access-token.duration}")
  private int accessTokenDuration;

  @Value("${jwt.refresh-token.duration}")
  private int refreshTokenDuration;

  public OAuth2SuccessHandler(
      AccessTokenService accessTokenService,
      RefreshTokenService refreshTokenService,
      DeviceService deviceService,
      CustomLogger customLogger) {
    this.accessTokenService = accessTokenService;
    this.refreshTokenService = refreshTokenService;
    this.deviceService = deviceService;
    this.customLogger = customLogger;
  }

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {
    final var userPrincipal = (OAuth2UserPrincipal) authentication.getPrincipal();
    final var roleDTOs =
        userPrincipal.getAuthorities().stream()
            .map(authority -> new RoleDTO(authority.getAuthority()))
            .collect(Collectors.toSet());
    final var deviceId = AuthUtil.generateDeviceId();
    final var accessToken = accessTokenService.create(userPrincipal.getId(), roleDTOs, deviceId);
    final var refreshToken = refreshTokenService.create(userPrincipal.getId(), roleDTOs, deviceId);
    final var userAgent = request.getHeader("User-Agent");
    deviceService.createOrUpdate(userPrincipal.getId(), deviceId, userAgent);
    final var accessTokenCookie =
        AuthUtil.createCookie("accessToken", accessToken, domain, accessTokenDuration);
    final var refreshTokenCookie =
        AuthUtil.createCookie("refreshToken", refreshToken, domain, refreshTokenDuration);
    response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
    response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
    response.sendRedirect(uiUrl);
    customLogger.info("Finished");
    MDC.remove("REQUEST_ID");
  }
}
