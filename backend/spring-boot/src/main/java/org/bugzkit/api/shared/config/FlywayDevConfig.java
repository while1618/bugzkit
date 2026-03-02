package org.bugzkit.api.shared.config;

import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
class FlywayDevConfig {

  @Bean
  FlywayMigrationStrategy cleanMigrate() {
    return flyway -> {
      flyway.clean();
      flyway.migrate();
    };
  }
}
