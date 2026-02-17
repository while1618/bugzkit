package org.bugzkit.api.auth.redis.model;

import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "ResetPasswordTokenStore")
public class ResetPasswordTokenStore implements Serializable {
  @Serial private static final long serialVersionUID = 4567890123456789012L;

  @Id private String token;

  @Indexed private Long userId;

  @TimeToLive private long timeToLive;
}
