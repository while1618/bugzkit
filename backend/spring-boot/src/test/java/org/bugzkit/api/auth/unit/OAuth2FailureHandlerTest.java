package org.bugzkit.api.auth.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.bugzkit.api.auth.oauth2.OAuth2FailureHandler;
import org.bugzkit.api.shared.logger.CustomLogger;
import org.bugzkit.api.shared.message.service.MessageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OAuth2FailureHandlerTest {
  @Mock private MessageService messageService;
  @Mock private CustomLogger customLogger;

  @InjectMocks private OAuth2FailureHandler oAuth2FailureHandler;

  @Test
  void onAuthenticationFailure_setsUnauthorizedStatusAndRedirects() throws Exception {
    ReflectionTestUtils.setField(oAuth2FailureHandler, "uiUrl", "http://localhost:3000");
    when(messageService.getMessage("user.notActive")).thenReturn("API_ERROR_USER_NOT_ACTIVE");

    final var request = new MockHttpServletRequest();
    final var response = new MockHttpServletResponse();
    final var exception = new DisabledException("user.notActive");

    oAuth2FailureHandler.onAuthenticationFailure(request, response, exception);

    assertEquals(
        "http://localhost:3000/auth/sign-in?error=API_ERROR_USER_NOT_ACTIVE",
        response.getRedirectedUrl());
  }

  @Test
  void onAuthenticationFailure_unknownError_fallsBackToInternalError() throws Exception {
    ReflectionTestUtils.setField(oAuth2FailureHandler, "uiUrl", "http://localhost:3000");
    when(messageService.getMessage("unknown.error")).thenThrow(new RuntimeException());
    when(messageService.getMessage("server.internalError"))
        .thenReturn("API_ERROR_INTERNAL_SERVER_ERROR");

    final var request = new MockHttpServletRequest();
    final var response = new MockHttpServletResponse();
    final var exception =
        new org.springframework.security.authentication.AuthenticationServiceException(
            "unknown.error");

    oAuth2FailureHandler.onAuthenticationFailure(request, response, exception);

    assertEquals(
        "http://localhost:3000/auth/sign-in?error=API_ERROR_INTERNAL_SERVER_ERROR",
        response.getRedirectedUrl());
  }
}
