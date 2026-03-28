package org.bugzkit.api.auth.oauth2;

import java.util.Map;
import org.bugzkit.api.auth.security.UserPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class OAuth2UserPrincipal extends UserPrincipal implements OAuth2User {
  private final Map<String, Object> attributes;

  public OAuth2UserPrincipal(UserPrincipal principal, Map<String, Object> attributes) {
    super(
        principal.getId(),
        principal.getUsername(),
        principal.getEmail(),
        principal.getPassword(),
        principal.isEnabled(),
        principal.isAccountNonLocked(),
        principal.getCreatedAt(),
        principal.getAuthorities());
    this.attributes = attributes;
  }

  @Override
  public String getName() {
    return this.getEmail();
  }

  @Override
  public Map<String, Object> getAttributes() {
    return this.attributes;
  }
}
