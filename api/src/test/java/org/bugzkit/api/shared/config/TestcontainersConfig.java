package org.bugzkit.api.shared.config;

import org.bugzkit.api.shared.util.MailpitClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@Sql(
    scripts = {"classpath:sql/truncate.sql", "classpath:sql/test-data.sql"},
    executionPhase = ExecutionPhase.BEFORE_TEST_CLASS)
public abstract class TestcontainersConfig {
  static PostgreSQLContainer postgres =
      new PostgreSQLContainer(DockerImageName.parse("postgres").withTag("latest"));

  static GenericContainer<?> redis =
      new GenericContainer<>(DockerImageName.parse("redis").withTag("latest"))
          .withExposedPorts(6379)
          .withCommand("redis-server", "--requirepass", "root");

  static GenericContainer<?> mailpit =
      new GenericContainer<>(DockerImageName.parse("axllent/mailpit").withTag("latest"))
          .withExposedPorts(1025, 8025);

  static {
    postgres.start();
    redis.start();
    mailpit.start();
  }

  protected static MailpitClient mailpit() {
    return new MailpitClient(mailpit.getHost(), mailpit.getMappedPort(8025));
  }

  @DynamicPropertySource
  static void containerProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.data.redis.host", redis::getHost);
    registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    registry.add("spring.mail.host", mailpit::getHost);
    registry.add("spring.mail.port", () -> mailpit.getMappedPort(1025));
  }
}
