package org.bugzkit.api.shared.config;

import org.bugzkit.api.shared.interceptor.RequestInterceptor;
import org.bugzkit.api.shared.ratelimit.RateLimitInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
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

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/**")
        .allowedOrigins(uiUrl)
        .allowedMethods("HEAD", "OPTIONS", "GET", "POST", "PUT", "PATCH", "DELETE")
        .allowCredentials(true)
        .maxAge(3600);
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(rateLimitInterceptor);
    registry.addInterceptor(requestInterceptor);
  }
}
