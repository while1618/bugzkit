package org.bugzkit.api.shared.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.bugzkit.api.shared.error.ErrorMessage;
import org.bugzkit.api.shared.message.service.MessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
  private final MessageService messageService;
  private final boolean enabled;
  private final LettuceBasedProxyManager<String> proxyManager;
  private final Map<String, BucketConfiguration> configCache = new ConcurrentHashMap<>();

  public RateLimitInterceptor(
      MessageService messageService,
      @Value("${rate-limit.enabled:true}") boolean enabled,
      RedisClient lettuceClient) {
    this.messageService = messageService;
    this.enabled = enabled;
    final var connection =
        lettuceClient.connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE));
    this.proxyManager =
        Bucket4jLettuce.casBasedBuilder(connection)
            .expirationAfterWrite(
                ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(
                    Duration.ofSeconds(60)))
            .build();
  }

  @Override
  public boolean preHandle(
      @Nonnull HttpServletRequest request,
      @Nonnull HttpServletResponse response,
      @Nonnull Object handler)
      throws Exception {
    if (!enabled) return true;
    if (!(handler instanceof HandlerMethod handlerMethod)) return true;
    final var rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);
    if (rateLimit == null) return true;

    final var ip = resolveClientIp(request);
    final var endpointKey =
        handlerMethod.getBeanType().getSimpleName() + ":" + handlerMethod.getMethod().getName();
    final var bucketKey = "rate-limit:" + endpointKey + ":" + ip;
    final var config =
        configCache.computeIfAbsent(endpointKey, k -> buildBucketConfiguration(rateLimit));
    final var bucket = proxyManager.getProxy(bucketKey, () -> config);
    final var probe = bucket.tryConsumeAndReturnRemaining(1);

    if (probe.isConsumed()) return true;

    writeRateLimitResponse(response, probe);
    return false;
  }

  private BucketConfiguration buildBucketConfiguration(RateLimit rateLimit) {
    final var bandwidth =
        Bandwidth.builder()
            .capacity(rateLimit.requests())
            .refillGreedy(rateLimit.requests(), Duration.ofSeconds(rateLimit.duration()))
            .build();
    return BucketConfiguration.builder().addLimit(bandwidth).build();
  }

  private String resolveClientIp(HttpServletRequest request) {
    final var forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isEmpty()) {
      return forwarded.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }

  private void writeRateLimitResponse(HttpServletResponse response, ConsumptionProbe probe)
      throws Exception {
    final var retryAfter = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill());
    final var status = HttpStatus.TOO_MANY_REQUESTS;
    final var errorMessage = new ErrorMessage(status);
    errorMessage.addCode(messageService.getMessage("request.tooManyRequests"));

    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    response.setHeader("Retry-After", String.valueOf(retryAfter));
    response.getWriter().write(errorMessage.toString());
  }
}
