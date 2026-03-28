package org.bugzkit.api.auth.redis.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RedisHash(value = "UserBlacklist")
public class UserBlacklist implements Serializable {
  @Serial private static final long serialVersionUID = 8334740937644612692L;

  @Id private Long userId;

  @Builder.Default private Instant updatedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS);

  @TimeToLive private long timeToLive;
}
