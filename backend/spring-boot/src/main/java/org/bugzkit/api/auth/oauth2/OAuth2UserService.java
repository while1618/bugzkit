package org.bugzkit.api.auth.oauth2;

import java.util.Collections;
import java.util.UUID;
import org.bugzkit.api.auth.security.UserPrincipal;
import org.bugzkit.api.shared.logger.CustomLogger;
import org.bugzkit.api.user.model.Role.RoleName;
import org.bugzkit.api.user.model.User;
import org.bugzkit.api.user.repository.RoleRepository;
import org.bugzkit.api.user.repository.UserRepository;
import org.slf4j.MDC;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class OAuth2UserService extends DefaultOAuth2UserService {
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final CustomLogger customLogger;

  public OAuth2UserService(
      UserRepository userRepository, RoleRepository roleRepository, CustomLogger customLogger) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.customLogger = customLogger;
  }

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    final var requestId = UUID.randomUUID().toString();
    MDC.put("REQUEST_ID", requestId);
    customLogger.info("Called");
    final var oAuthUser = super.loadUser(userRequest);
    final String email = oAuthUser.getAttribute("email");
    final boolean emailVerified = Boolean.TRUE.equals(oAuthUser.getAttribute("email_verified"));
    if (!emailVerified) throw new DisabledException("user.notActive");
    final var user = userRepository.findWithRolesByEmail(email).orElseGet(() -> createUser(email));
    if (user.getUsername() == null) user.setUsername(email);
    return new OAuth2UserPrincipal(UserPrincipal.create(user), oAuthUser.getAttributes());
  }

  private User createUser(String email) {
    final var roles =
        roleRepository
            .findByName(RoleName.USER)
            .orElseThrow(() -> new RuntimeException("user.roleNotFound"));
    final var user =
        User.builder().email(email).active(true).roles(Collections.singleton(roles)).build();
    return userRepository.save(user);
  }
}
