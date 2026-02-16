package org.bugzkit.api.shared.util;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.bugzkit.api.auth.AuthTokens;
import org.bugzkit.api.auth.payload.request.AuthTokensRequest;
import org.bugzkit.api.shared.constants.Path;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

public class IntegrationTestUtil {
  private IntegrationTestUtil() {}

  public static AuthTokens authTokens(MockMvc mockMvc, ObjectMapper objectMapper, String username)
      throws Exception {
    final var authTokensRequest = new AuthTokensRequest(username, "qwerty123");
    final var response =
        mockMvc
            .perform(
                post(Path.AUTH + "/tokens")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(authTokensRequest)))
            .andExpect(status().isNoContent())
            .andReturn()
            .getResponse();
    return new AuthTokens(
        response.getCookie("accessToken").getValue(),
        response.getCookie("refreshToken").getValue(),
        response.getCookie("deviceId").getValue());
  }
}
