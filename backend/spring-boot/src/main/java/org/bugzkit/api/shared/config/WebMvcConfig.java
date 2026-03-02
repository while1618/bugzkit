package org.bugzkit.api.shared.config;

import java.util.List;
import org.bugzkit.api.shared.interceptor.RequestInterceptor;
import org.bugzkit.api.shared.ratelimit.RateLimitInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
  @Value("${ui.url}")
  private String uiUrl;

  private final RateLimitInterceptor rateLimitInterceptor;
  private final RequestInterceptor requestInterceptor;

  public WebMvcConfig(
      RateLimitInterceptor rateLimitInterceptor, RequestInterceptor requestInterceptor) {
    this.rateLimitInterceptor = rateLimitInterceptor;
    this.requestInterceptor = requestInterceptor;
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    final var config = new CorsConfiguration();
    config.setAllowedOrigins(List.of(uiUrl));
    config.setAllowedMethods(List.of("HEAD", "OPTIONS", "GET", "POST", "PUT", "PATCH", "DELETE"));
    config.setAllowCredentials(true);
    config.setMaxAge(3600L);
    final var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(rateLimitInterceptor);
    registry.addInterceptor(requestInterceptor);
  }
}
