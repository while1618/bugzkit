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
@RedisHash(value = "RefreshTokenStore")
public class RefreshTokenStore implements Serializable {
  @Serial private static final long serialVersionUID = -1997218842142407911L;

  @Id private String refreshToken;

  @Indexed private Long userId;

  @Indexed private String deviceId;

  @TimeToLive private long timeToLive;
}
