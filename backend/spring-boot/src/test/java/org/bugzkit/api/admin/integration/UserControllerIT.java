package org.bugzkit.api.admin.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.time.LocalDateTime;
import java.util.Set;
import org.bugzkit.api.admin.payload.request.PatchUserRequest;
import org.bugzkit.api.admin.payload.request.UserRequest;
import org.bugzkit.api.shared.config.DatabaseContainers;
import org.bugzkit.api.shared.constants.Path;
import org.bugzkit.api.shared.util.IntegrationTestUtil;
import org.bugzkit.api.user.model.Role.RoleName;
import org.bugzkit.api.user.payload.dto.RoleDTO;
import org.bugzkit.api.user.payload.dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
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
class UserControllerIT extends DatabaseContainers {
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  private String accessToken;

  @BeforeEach
  void setUp() throws Exception {
    accessToken = IntegrationTestUtil.authTokens(mockMvc, objectMapper, "admin").accessToken();
  }

  @Test
  void createUser() throws Exception {
    final var userRequest =
        UserRequest.builder()
            .username("test1")
            .email("test1@localhost")
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
                .cookie(new Cookie("accessToken", accessToken))
                .content(objectMapper.writeValueAsString(userRequest)))
        .andExpect(status().isCreated());
  }

  @Test
  void createUser_throwBadRequest_invalidParameters() throws Exception {
    final var userRequest =
        UserRequest.builder()
            .username("invalid#$%")
            .email("invalid")
            .password("qwerty123")
            .confirmPassword("qwerty1234")
            .active(true)
            .lock(false)
            .roleNames(Set.of(RoleName.USER))
            .build();
    mockMvc
        .perform(
            post(Path.ADMIN_USERS)
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(new Cookie("accessToken", accessToken))
                .content(objectMapper.writeValueAsString(userRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("API_ERROR_USER_USERNAME_INVALID")))
        .andExpect(content().string(containsString("API_ERROR_USER_EMAIL_INVALID")))
        .andExpect(content().string(containsString("API_ERROR_USER_PASSWORDS_DO_NOT_MATCH")));
  }

  @Test
  void createUser_throwConflict_usernameExists() throws Exception {
    final var userRequest =
        UserRequest.builder()
            .username("admin")
            .email("test2@localhost")
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
                .cookie(new Cookie("accessToken", accessToken))
                .content(objectMapper.writeValueAsString(userRequest)))
        .andExpect(status().isConflict())
        .andExpect(content().string(containsString("API_ERROR_USER_USERNAME_EXISTS")));
  }

  @Test
  void createUser_throwConflict_emailExists() throws Exception {
    final var userRequest =
        UserRequest.builder()
            .username("test2")
            .email("admin@localhost")
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
                .cookie(new Cookie("accessToken", accessToken))
                .content(objectMapper.writeValueAsString(userRequest)))
        .andExpect(status().isConflict())
        .andExpect(content().string(containsString("API_ERROR_USER_EMAIL_EXISTS")));
  }

  @Test
  void findAllUsers() throws Exception {
    mockMvc
        .perform(
            get(Path.ADMIN_USERS)
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(new Cookie("accessToken", accessToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(10))
        .andExpect(jsonPath("$.total").value(11));
  }

  @Test
  void findUserById() throws Exception {
    final var expectedUserDTO =
        UserDTO.builder()
            .id(2L)
            .username("user")
            .email("user@localhost")
            .active(true)
            .lock(false)
            .roleDTOs(Set.of(new RoleDTO(RoleName.USER.name())))
            .createdAt(LocalDateTime.now())
            .build();
    final var response =
        mockMvc
            .perform(
                get(Path.ADMIN_USERS + "/{id}", 2L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .cookie(new Cookie("accessToken", accessToken)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    final var actualUserDTO = objectMapper.readValue(response, UserDTO.class);
    assertThat(actualUserDTO).isEqualTo(expectedUserDTO);
  }

  @Test
  void findUserById_throwResourceNotFound() throws Exception {
    mockMvc
        .perform(
            get(Path.ADMIN_USERS + "/{id}", 100L)
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(new Cookie("accessToken", accessToken)))
        .andExpect(status().isNotFound())
        .andExpect(content().string(containsString("API_ERROR_USER_NOT_FOUND")));
  }

  @Test
  void updateUser() throws Exception {
    final var userRequest =
        UserRequest.builder()
            .username("test3")
            .email("test3@localhost")
            .password("qwerty123")
            .confirmPassword("qwerty123")
            .active(true)
            .lock(false)
            .roleNames(Set.of(RoleName.USER))
            .build();
    mockMvc
        .perform(
            put(Path.ADMIN_USERS + "/{id}", 5L)
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(new Cookie("accessToken", accessToken))
                .content(objectMapper.writeValueAsString(userRequest)))
        .andExpect(status().isOk());
  }

  @Test
  void updateUser_throwBadRequest_invalidParameters() throws Exception {
    final var userRequest =
        UserRequest.builder()
            .username("invalid#$%")
            .email("invalid")
            .password("qwerty123")
            .confirmPassword("qwerty1234")
            .active(true)
            .lock(false)
            .roleNames(Set.of(RoleName.USER))
            .build();
    mockMvc
        .perform(
            put(Path.ADMIN_USERS + "/{id}", 5L)
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(new Cookie("accessToken", accessToken))
                .content(objectMapper.writeValueAsString(userRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("API_ERROR_USER_USERNAME_INVALID")))
        .andExpect(content().string(containsString("API_ERROR_USER_EMAIL_INVALID")))
        .andExpect(content().string(containsString("API_ERROR_USER_PASSWORDS_DO_NOT_MATCH")));
  }

  @Test
  void updateUser_throwConflict_usernameExists() throws Exception {
    final var userRequest =
        UserRequest.builder()
            .username("admin")
            .email("test2@localhost")
            .password("qwerty123")
            .confirmPassword("qwerty123")
            .active(true)
            .lock(false)
            .roleNames(Set.of(RoleName.USER))
            .build();
    mockMvc
        .perform(
            put(Path.ADMIN_USERS + "/{id}", 5L)
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(new Cookie("accessToken", accessToken))
                .content(objectMapper.writeValueAsString(userRequest)))
        .andExpect(status().isConflict())
        .andExpect(content().string(containsString("API_ERROR_USER_USERNAME_EXISTS")));
  }

  @Test
  void updateUser_throwConflict_emailExists() throws Exception {
    final var userRequest =
        UserRequest.builder()
            .username("test2")
            .email("admin@localhost")
            .password("qwerty123")
            .confirmPassword("qwerty123")
            .active(true)
            .lock(false)
            .roleNames(Set.of(RoleName.USER))
            .build();
    mockMvc
        .perform(
            put(Path.ADMIN_USERS + "/{id}", 5L)
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(new Cookie("accessToken", accessToken))
                .content(objectMapper.writeValueAsString(userRequest)))
        .andExpect(status().isConflict())
        .andExpect(content().string(containsString("API_ERROR_USER_EMAIL_EXISTS")));
  }

  @Test
  void patchUser() throws Exception {
    final var patchUserRequest =
        PatchUserRequest.builder()
            .username("test4")
            .email("test4@localhost")
            .password("qwerty1234")
            .confirmPassword("qwerty1234")
            .active(true)
            .lock(false)
            .roleNames(Set.of(RoleName.USER, RoleName.ADMIN))
            .build();
    mockMvc
        .perform(
            patch(Path.ADMIN_USERS + "/{id}", 5L)
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(new Cookie("accessToken", accessToken))
                .content(objectMapper.writeValueAsString(patchUserRequest)))
        .andExpect(status().isOk());
  }

  @Test
  void patchUser_throwBadRequest_invalidParameters() throws Exception {
    final var patchUserRequest =
        PatchUserRequest.builder()
            .username("invalid#$%")
            .email("invalid")
            .password("qwerty123")
            .confirmPassword("qwerty1234")
            .active(true)
            .lock(false)
            .roleNames(Set.of(RoleName.USER))
            .build();
    mockMvc
        .perform(
            patch(Path.ADMIN_USERS + "/{id}", 5L)
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(new Cookie("accessToken", accessToken))
                .content(objectMapper.writeValueAsString(patchUserRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("API_ERROR_USER_USERNAME_INVALID")))
        .andExpect(content().string(containsString("API_ERROR_USER_EMAIL_INVALID")))
        .andExpect(content().string(containsString("API_ERROR_USER_PASSWORDS_DO_NOT_MATCH")));
  }

  @Test
  void patchUser_throwConflict_usernameExists() throws Exception {
    final var patchUserRequest =
        PatchUserRequest.builder()
            .username("admin")
            .email("test2@localhost")
            .password("qwerty123")
            .confirmPassword("qwerty123")
            .active(true)
            .lock(false)
            .roleNames(Set.of(RoleName.USER))
            .build();
    mockMvc
        .perform(
            patch(Path.ADMIN_USERS + "/{id}", 5L)
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(new Cookie("accessToken", accessToken))
                .content(objectMapper.writeValueAsString(patchUserRequest)))
        .andExpect(status().isConflict())
        .andExpect(content().string(containsString("API_ERROR_USER_USERNAME_EXISTS")));
  }

  @Test
  void patchUser_throwConflict_emailExists() throws Exception {
    final var patchUserRequest =
        PatchUserRequest.builder()
            .username("test2")
            .email("admin@localhost")
            .password("qwerty123")
            .confirmPassword("qwerty123")
            .active(true)
            .lock(false)
            .roleNames(Set.of(RoleName.USER))
            .build();
    mockMvc
        .perform(
            patch(Path.ADMIN_USERS + "/{id}", 5L)
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(new Cookie("accessToken", accessToken))
                .content(objectMapper.writeValueAsString(patchUserRequest)))
        .andExpect(status().isConflict())
        .andExpect(content().string(containsString("API_ERROR_USER_EMAIL_EXISTS")));
  }

  @Test
  void deleteUser() throws Exception {
    mockMvc
        .perform(
            delete(Path.ADMIN_USERS + "/{id}", 10L)
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(new Cookie("accessToken", accessToken)))
        .andExpect(status().isNoContent());
  }
}
