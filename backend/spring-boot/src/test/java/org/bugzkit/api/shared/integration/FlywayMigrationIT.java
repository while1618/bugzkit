package org.bugzkit.api.shared.integration;

import org.bugzkit.api.shared.config.TestcontainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class FlywayMigrationIT extends TestcontainersConfig {

  @Test
  void migrationRunsAndSchemaIsValid() {}
}
