package org.bugzkit.api.shared.integration;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.bugzkit.api.auth.payload.request.ForgotPasswordRequest;
import org.bugzkit.api.auth.payload.request.VerificationEmailRequest;
import org.bugzkit.api.shared.config.DatabaseContainers;
import org.bugzkit.api.shared.constants.Path;
import org.bugzkit.api.shared.email.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@DirtiesContext
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest
@TestPropertySource(properties = "rate-limit.enabled=true")
class RateLimitIT extends DatabaseContainers {
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockitoBean private EmailService emailService;

  @Value("${server.client-ip-header}")
  private String clientIpHeader;

  @Test
  void forgotPassword_withinLimitSucceeds_exceedLimitReturns429() throws Exception {
    final var body = objectMapper.writeValueAsString(new ForgotPasswordRequest("test@example.com"));

    // 3 requests within limit should succeed
    for (int i = 0; i < 3; i++) {
      mockMvc
          .perform(
              post(Path.AUTH + "/password/forgot")
                  .header(clientIpHeader, "10.0.0.1")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(body))
          .andExpect(status().isNoContent());
    }

    // 4th request should be rate limited
    mockMvc
        .perform(
            post(Path.AUTH + "/password/forgot")
                .header(clientIpHeader, "10.0.0.1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isTooManyRequests())
        .andExpect(content().string(containsString("API_ERROR_REQUEST_TOO_MANY_REQUESTS")))
        .andExpect(header().exists("Retry-After"));
  }

  @Test
  void rateLimitedEndpoints_haveIndependentBuckets() throws Exception {
    final var forgotBody =
        objectMapper.writeValueAsString(new ForgotPasswordRequest("test@example.com"));
    final var verificationBody =
        objectMapper.writeValueAsString(new VerificationEmailRequest("test@example.com"));

    // Exhaust forgot-password limit (3 requests)
    for (int i = 0; i < 3; i++) {
      mockMvc
          .perform(
              post(Path.AUTH + "/password/forgot")
                  .header(clientIpHeader, "10.0.0.2")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(forgotBody))
          .andExpect(status().isNoContent());
    }

    // Forgot password is exhausted
    mockMvc
        .perform(
            post(Path.AUTH + "/password/forgot")
                .header(clientIpHeader, "10.0.0.2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(forgotBody))
        .andExpect(status().isTooManyRequests());

    // Verification email should still work (separate bucket)
    mockMvc
        .perform(
            post(Path.AUTH + "/verification-email")
                .header(clientIpHeader, "10.0.0.2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(verificationBody))
        .andExpect(status().isNoContent());
  }

  @Test
  void rateLimitIsPerIp_differentIpsHaveIndependentBuckets() throws Exception {
    final var body = objectMapper.writeValueAsString(new ForgotPasswordRequest("test@example.com"));

    // Exhaust limit for IP 10.0.0.3
    for (int i = 0; i < 3; i++) {
      mockMvc
          .perform(
              post(Path.AUTH + "/password/forgot")
                  .header(clientIpHeader, "10.0.0.3")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(body))
          .andExpect(status().isNoContent());
    }

    // 10.0.0.3 is rate limited
    mockMvc
        .perform(
            post(Path.AUTH + "/password/forgot")
                .header(clientIpHeader, "10.0.0.3")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isTooManyRequests());

    // 10.0.0.4 should still work (different IP, different bucket)
    mockMvc
        .perform(
            post(Path.AUTH + "/password/forgot")
                .header(clientIpHeader, "10.0.0.4")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isNoContent());
  }

  @Test
  void endpointWithoutRateLimit_neverThrottled() throws Exception {
    // DELETE /auth/tokens has no @RateLimit annotation
    for (int i = 0; i < 20; i++) {
      mockMvc
          .perform(
              delete(Path.AUTH + "/tokens")
                  .header(clientIpHeader, "10.0.0.5")
                  .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isNoContent());
    }
  }
}
