package org.bugzkit.api.shared.config;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import org.bugzkit.api.shared.error.ErrorMessage;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {

  @Value("${app.name}")
  private String appName;

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title(appName)
                .description("A production-ready web application template")
                .version("1.0.0")
                .license(
                    new License()
                        .name("MIT License")
                        .url("https://choosealicense.com/licenses/mit/")))
        .tags(
            List.of(
                new Tag()
                    .name("auth")
                    .description(
                        "Registration, sign-in, token management, device management, password reset, and email verification"),
                new Tag()
                    .name("profile")
                    .description(
                        "Manage the authenticated user's own profile, password, and account"),
                new Tag()
                    .name("users")
                    .description("Public user lookup and username/email availability checks"),
                new Tag().name("admin").description("Admin user management — requires ADMIN role"),
                new Tag().name("roles").description("Role listing — requires ADMIN role")))
        .components(
            new Components()
                .addSecuritySchemes(
                    "cookieAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.COOKIE)
                        .name("accessToken")
                        .description("HTTP-only JWT access token set by the sign-in endpoint")));
  }

  @Bean
  public OpenApiCustomizer errorMessageSchema() {
    return openApi ->
        openApi
            .getComponents()
            .getSchemas()
            .putAll(ModelConverters.getInstance().readAll(new AnnotatedType(ErrorMessage.class)));
  }
}
