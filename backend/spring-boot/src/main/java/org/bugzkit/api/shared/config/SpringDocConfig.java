package org.bugzkit.api.shared.config;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.bugzkit.api.shared.error.ErrorMessage;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("bugzkit")
                .description("A production-ready web application template")
                .version("1.0.0")
                .contact(new Contact().email("office@bugzkit.com"))
                .license(
                    new License()
                        .name("MIT License")
                        .url("https://choosealicense.com/licenses/mit/")))
        .components(
            new Components()
                .addSecuritySchemes(
                    "cookieAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.COOKIE)
                        .name("accessToken")));
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
