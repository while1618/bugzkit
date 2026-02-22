package org.bugzkit.api.shared.config;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisKeyValueAdapter.EnableKeyspaceEvents;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@Profile({"dev", "prod", "test"})
@EnableRedisRepositories(enableKeyspaceEvents = EnableKeyspaceEvents.ON_STARTUP)
public class RedisConfig {
  @Value("${spring.data.redis.host}")
  private String host;

  @Value("${spring.data.redis.port}")
  private int port;

  @Value("${spring.data.redis.database}")
  private int database;

  @Value("${spring.data.redis.password}")
  private String password;

  @Value("${spring.data.redis.timeout}")
  private int timeout;

  @Bean
  LettuceConnectionFactory redisConnectionFactory() {
    final var redisConfig = new RedisStandaloneConfiguration();
    redisConfig.setHostName(host);
    redisConfig.setPort(port);
    redisConfig.setDatabase(database);
    redisConfig.setPassword(RedisPassword.of(password));
    final var clientConfig =
        LettuceClientConfiguration.builder().commandTimeout(Duration.ofSeconds(timeout)).build();
    return new LettuceConnectionFactory(redisConfig, clientConfig);
  }

  @Bean
  public RedisTemplate<String, Object> redisTemplate() {
    final var template = new RedisTemplate<String, Object>();
    template.setConnectionFactory(redisConnectionFactory());
    return template;
  }

  @Bean(destroyMethod = "shutdown")
  public RedisClient lettuceClient() {
    final var redisURI =
        RedisURI.builder()
            .withHost(host)
            .withPort(port)
            .withDatabase(database)
            .withPassword(password.toCharArray())
            .withTimeout(Duration.ofSeconds(timeout))
            .build();
    return RedisClient.create(redisURI);
  }
}
