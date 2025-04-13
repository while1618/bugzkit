package org.bugzkit.api.auth.security;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.bugzkit.api.auth.jwt.service.AccessTokenService;
import org.bugzkit.api.auth.jwt.util.JwtUtil;
import org.bugzkit.api.auth.util.AuthUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JWTFilter extends OncePerRequestFilter {
  private final AccessTokenService accessTokenService;
  private final UserDetailsServiceImpl userDetailsService;

  public JWTFilter(
      AccessTokenService accessTokenService, UserDetailsServiceImpl userDetailsService) {
    this.accessTokenService = accessTokenService;
    this.userDetailsService = userDetailsService;
  }

  @Override
  protected void doFilterInternal(
      @Nonnull HttpServletRequest request,
      @Nonnull HttpServletResponse response,
      @Nonnull FilterChain chain)
      throws IOException, ServletException {
    final var accessToken = AuthUtil.getValueFromCookie("accessToken", request);
    if (accessToken == null) {
      chain.doFilter(request, response);
      return;
    }
    try {
      SecurityContextHolder.getContext().setAuthentication(getAuth(accessToken));
    } catch (RuntimeException ignored) {
    } finally {
      chain.doFilter(request, response);
    }
  }

  private UsernamePasswordAuthenticationToken getAuth(String accessToken) {
    accessTokenService.check(accessToken);
    final var userId = JwtUtil.getUserId(accessToken);
    final var userPrincipal = (UserPrincipal) userDetailsService.loadUserByUserId(userId);
    return new UsernamePasswordAuthenticationToken(
        userPrincipal, null, userPrincipal.getAuthorities());
  }
}
