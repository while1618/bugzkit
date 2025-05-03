package org.bugzkit.api.auth.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import org.bugzkit.api.auth.security.UserPrincipal;
import org.bugzkit.api.user.model.Role.RoleName;
import org.springframework.boot.web.server.Cookie.SameSite;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthUtil {
  private AuthUtil() {}

  public static boolean isSignedIn() {
    final var auth = SecurityContextHolder.getContext().getAuthentication();
    return !auth.getPrincipal().equals("anonymousUser");
  }

  public static boolean isAdminSignedIn() {
    if (!isSignedIn()) return false;
    final var userPrincipal = findSignedInUser();
    return userPrincipal.getAuthorities().stream()
        .anyMatch(authority -> authority.getAuthority().equals(RoleName.ADMIN.name()));
  }

  public static UserPrincipal findSignedInUser() {
    final var auth = SecurityContextHolder.getContext().getAuthentication();
    return (UserPrincipal) auth.getPrincipal();
  }

  public static String getAuthName() {
    final var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || auth.getPrincipal().equals("anonymousUser")) return "anonymousUser";
    return auth.getName();
  }

  public static String getUserIpAddress(HttpServletRequest request) {
    final var ipAddress = request.getHeader("x-forwarded-for");
    if (ipAddress == null || ipAddress.isEmpty()) return request.getRemoteAddr();
    return ipAddress;
  }

  public static String getValueFromCookie(String name, HttpServletRequest request) {
    final var cookies = request.getCookies();
    if (cookies == null) return null;
    return Arrays.stream(cookies)
        .filter(cookie -> name.equals(cookie.getName()))
        .findFirst()
        .map(Cookie::getValue)
        .orElse(null);
  }

  public static ResponseCookie createCookie(String name, String value, int maxAge) {
    return ResponseCookie.from(name, value)
        .httpOnly(true)
        .secure(true)
        .domain(".bugzkit.com")
        .path("/")
        .maxAge(maxAge)
        .sameSite(SameSite.NONE.attributeValue())
        .build();
  }
}
