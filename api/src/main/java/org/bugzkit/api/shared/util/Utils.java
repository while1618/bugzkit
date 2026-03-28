package org.bugzkit.api.shared.util;

import jakarta.servlet.http.HttpServletRequest;

public final class Utils {
  private Utils() {}

  public static String resolveClientIp(HttpServletRequest request, String clientIpHeader) {
    if (clientIpHeader != null && !clientIpHeader.isBlank()) {
      final var ip = request.getHeader(clientIpHeader);
      if (ip != null && !ip.isBlank()) {
        return ip;
      }
    }
    return request.getRemoteAddr();
  }
}
