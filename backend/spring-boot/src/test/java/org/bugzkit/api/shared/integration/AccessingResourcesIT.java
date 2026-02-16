package org.bugzkit.api.shared.integration;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.util.Set;
import org.bugzkit.api.admin.payload.request.PatchUserRequest;
import org.bugzkit.api.admin.payload.request.UserRequest;
import org.bugzkit.api.shared.config.DatabaseContainers;
import org.bugzkit.api.shared.constants.Path;
import org.bugzkit.api.shared.util.IntegrationTestUtil;
import org.bugzkit.api.user.model.Role.RoleName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@DirtiesContext
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest
class AccessingResourcesIT extends DatabaseContainers {
  private final String unauthorized = "API_ERROR_AUTH_UNAUTHORIZED";
  private final String forbidden = "API_ERROR_AUTH_FORBIDDEN";
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void getProfile_throwUnauthorized_userNotSignedIn() throws Exception {
    mockMvc
        .perform(get(Path.PROFILE).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized())
        .andExpect(content().string(containsString(unauthorized)));
  }

  @Test
  void patchProfile_throwUnauthorized_userNotSignedIn() throws Exception {
    mockMvc
        .perform(patch(Path.PROFILE).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized())
        .andExpect(content().string(containsString(unauthorized)));
  }

  @Test
  void deleteProfile_throwUnauthorized_userNotSignedIn() throws Exception {
    mockMvc
        .perform(delete(Path.PROFILE).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized())
        .andExpect(content().string(containsString(unauthorized)));
  }

  @Test
  void changePassword_throwUnauthorized_userNotSignedIn() throws Exception {
    mockMvc
        .perform(patch(Path.PROFILE + "/password").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized())
        .andExpect(content().string(containsString(unauthorized)));
  }

  @Test
  void createUser_throwUnauthorized_userNotSignedIn() throws Exception {
    mockMvc
        .perform(post(Path.ADMIN_USERS).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized())
        .andExpect(content().string(containsString(unauthorized)));
  }

  @Test
  void createUser_throwForbidden_userNotAdmin() throws Exception {
    final var authTokens = IntegrationTestUtil.authTokens(mockMvc, objectMapper, "user");
    final var userRequest =
        UserRequest.builder()
            .username("test")
            .email("test@localhost")
            .password("qwerty123")
            .confirmPassword("qwerty123")
            .active(true)
            .lock(false)
            .roleNames(Set.of(RoleName.USER))
            .build();
    mockMvc
        .perform(
            post(Path.ADMIN_USERS)
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(new Cookie("accessToken", authTokens.accessToken()))
                .content(objectMapper.writeValueAsString(userRequest)))
        .andExpect(status().isForbidden())
        .andExpect(content().string(containsString(forbidden)));
  }

  @Test
  void getUsers_throwUnauthorized_userNotSignedIn() throws Exception {
    mockMvc
        .perform(get(Path.ADMIN_USERS).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized())
        .andExpect(content().string(containsString(unauthorized)));
  }

  @Test
  void getUsers_throwForbidden_userNotAdmin() throws Exception {
    final var authTokens = IntegrationTestUtil.authTokens(mockMvc, objectMapper, "user");
    mockMvc
        .perform(
            get(Path.ADMIN_USERS)
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(new Cookie("accessToken", authTokens.accessToken())))
        .andExpect(status().isForbidden())
        .andExpect(content().string(containsString(forbidden)));
  }

  @Test
  void getUserById_throwUnauthorized_userNotSignedIn() throws Exception {
    mockMvc
        .perform(get(Path.ADMIN_USERS + "/{id}", 1L).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized())
        .andExpect(content().string(containsString(unauthorized)));
  }

  @Test
  void getUserById_throwForbidden_userNotAdmin() throws Exception {
    final var authTokens = IntegrationTestUtil.authTokens(mockMvc, objectMapper, "user");
    mockMvc
        .perform(
            get(Path.ADMIN_USERS + "/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(new Cookie("accessToken", authTokens.accessToken())))
        .andExpect(status().isForbidden())
        .andExpect(content().string(containsString(forbidden)));
  }

  @Test
  void updateUserById_throwUnauthorized_userNotSignedIn() throws Exception {
    mockMvc
        .perform(put(Path.ADMIN_USERS + "/{id}", 1L).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized())
        .andExpect(content().string(containsString(unauthorized)));
  }

  @Test
  void updateUserById_throwForbidden_userNotAdmin() throws Exception {
    final var authTokens = IntegrationTestUtil.authTokens(mockMvc, objectMapper, "user");
    final var userRequest =
        UserRequest.builder()
            .username("test")
            .email("test@localhost")
            .password("qwerty123")
            .confirmPassword("qwerty123")
            .active(true)
            .lock(false)
            .roleNames(Set.of(RoleName.USER))
            .build();
    mockMvc
        .perform(
            put(Path.ADMIN_USERS + "/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(new Cookie("accessToken", authTokens.accessToken()))
                .content(objectMapper.writeValueAsString(userRequest)))
        .andExpect(status().isForbidden())
        .andExpect(content().string(containsString(forbidden)));
  }

  @Test
  void patchUserById_throwUnauthorized_userNotSignedIn() throws Exception {
    mockMvc
        .perform(patch(Path.ADMIN_USERS + "/{id}", 1L).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized())
        .andExpect(content().string(containsString(unauthorized)));
  }

  @Test
  void patchUserById_throwForbidden_userNotAdmin() throws Exception {
    final var authTokens = IntegrationTestUtil.authTokens(mockMvc, objectMapper, "user");
    final var patchUserRequest = PatchUserRequest.builder().build();
    mockMvc
        .perform(
            patch(Path.ADMIN_USERS + "/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(new Cookie("accessToken", authTokens.accessToken()))
                .content(objectMapper.writeValueAsString(patchUserRequest)))
        .andExpect(status().isForbidden())
        .andExpect(content().string(containsString(forbidden)));
  }

  @Test
  void deleteUserById_throwUnauthorized_userNotSignedIn() throws Exception {
    mockMvc
        .perform(delete(Path.ADMIN_USERS + "/{id}", 1L).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized())
        .andExpect(content().string(containsString(unauthorized)));
  }

  @Test
  void deleteUserById_throwForbidden_userNotAdmin() throws Exception {
    final var authTokens = IntegrationTestUtil.authTokens(mockMvc, objectMapper, "user");
    mockMvc
        .perform(
            delete(Path.ADMIN_USERS + "/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(new Cookie("accessToken", authTokens.accessToken())))
        .andExpect(status().isForbidden())
        .andExpect(content().string(containsString(forbidden)));
  }

  @Test
  void getRoles_throwUnauthorized_userNotSignedIn() throws Exception {
    mockMvc
        .perform(get(Path.ROLES).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized())
        .andExpect(content().string(containsString(unauthorized)));
  }

  @Test
  void getRoles_throwForbidden_userNotAdmin() throws Exception {
    final var authTokens = IntegrationTestUtil.authTokens(mockMvc, objectMapper, "user");
    mockMvc
        .perform(
            get(Path.ROLES)
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(new Cookie("accessToken", authTokens.accessToken())))
        .andExpect(status().isForbidden())
        .andExpect(content().string(containsString(forbidden)));
  }

  @Test
  void findAllDevices_throwUnauthorized_userNotSignedIn() throws Exception {
    mockMvc
        .perform(get(Path.AUTH + "/tokens/devices").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized())
        .andExpect(content().string(containsString(unauthorized)));
  }

  @Test
  void deleteDevice_throwUnauthorized_userNotSignedIn() throws Exception {
    mockMvc
        .perform(
            delete(Path.AUTH + "/tokens/devices/{deviceId}", "some-device-id")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized())
        .andExpect(content().string(containsString(unauthorized)));
  }
}
