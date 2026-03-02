package org.bugzkit.api.shared.integration;

import org.bugzkit.api.shared.config.DatabaseContainers;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class FlywayMigrationIT extends DatabaseContainers {

  @Test
  void migrationRunsAndSchemaIsValid() {}
}
