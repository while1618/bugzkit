package org.bugzkit.api.auth.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.bugzkit.api.auth.jwt.service.AccessTokenService;
import org.bugzkit.api.auth.jwt.service.RefreshTokenService;
import org.bugzkit.api.auth.oauth2.OAuth2SuccessHandler;
import org.bugzkit.api.auth.oauth2.OAuth2UserPrincipal;
import org.bugzkit.api.auth.security.UserPrincipal;
import org.bugzkit.api.auth.service.DeviceService;
import org.bugzkit.api.shared.logger.CustomLogger;
import org.bugzkit.api.user.model.Role;
import org.bugzkit.api.user.model.Role.RoleName;
import org.bugzkit.api.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OAuth2SuccessHandlerTest {
  @Mock private AccessTokenService accessTokenService;
  @Mock private RefreshTokenService refreshTokenService;
  @Mock private DeviceService deviceService;
  @Mock private CustomLogger customLogger;
  @Mock private Authentication authentication;

  @InjectMocks private OAuth2SuccessHandler oAuth2SuccessHandler;

  @Test
  void onAuthenticationSuccess_setsCookiesAndRedirects() throws Exception {
    ReflectionTestUtils.setField(oAuth2SuccessHandler, "domain", "localhost");
    ReflectionTestUtils.setField(oAuth2SuccessHandler, "uiUrl", "http://localhost:3000");
    ReflectionTestUtils.setField(oAuth2SuccessHandler, "accessTokenDuration", 900);
    ReflectionTestUtils.setField(oAuth2SuccessHandler, "refreshTokenDuration", 604800);

    final var user =
        User.builder()
            .id(1L)
            .email("oauth@gmail.com")
            .active(true)
            .lock(false)
            .roles(Collections.singleton(new Role(RoleName.USER)))
            .build();
    final var principal = new OAuth2UserPrincipal(UserPrincipal.create(user), Map.of("sub", "123"));
    when(authentication.getPrincipal()).thenReturn(principal);
    when(accessTokenService.create(any(), any(), any())).thenReturn("access-token-value");
    when(refreshTokenService.create(any(), any(), any())).thenReturn("refresh-token-value");

    final var request = new MockHttpServletRequest();
    request.addHeader("User-Agent", "TestBrowser/1.0");
    final var response = new MockHttpServletResponse();

    oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication);

    assertTrue(response.getHeader("Set-Cookie").contains("accessToken=access-token-value"));
    assertEquals("http://localhost:3000", response.getRedirectedUrl());
    verify(deviceService).createOrUpdate(any(), any(), any());
  }

  @Test
  void onAuthenticationSuccess_createsTokensWithCorrectRoles() throws Exception {
    ReflectionTestUtils.setField(oAuth2SuccessHandler, "domain", "localhost");
    ReflectionTestUtils.setField(oAuth2SuccessHandler, "uiUrl", "http://localhost:3000");
    ReflectionTestUtils.setField(oAuth2SuccessHandler, "accessTokenDuration", 900);
    ReflectionTestUtils.setField(oAuth2SuccessHandler, "refreshTokenDuration", 604800);

    final var user =
        User.builder()
            .id(5L)
            .email("admin-oauth@gmail.com")
            .active(true)
            .lock(false)
            .roles(Set.of(new Role(RoleName.USER), new Role(RoleName.ADMIN)))
            .build();
    final var principal = new OAuth2UserPrincipal(UserPrincipal.create(user), Map.of("sub", "456"));
    when(authentication.getPrincipal()).thenReturn(principal);
    when(accessTokenService.create(any(), any(), any())).thenReturn("access-token");
    when(refreshTokenService.create(any(), any(), any())).thenReturn("refresh-token");

    final var request = new MockHttpServletRequest();
    request.addHeader("User-Agent", "TestBrowser/1.0");
    final var response = new MockHttpServletResponse();

    oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication);

    verify(accessTokenService).create(any(Long.class), any(Set.class), any(String.class));
    verify(refreshTokenService).create(any(Long.class), any(Set.class), any(String.class));
  }
}
