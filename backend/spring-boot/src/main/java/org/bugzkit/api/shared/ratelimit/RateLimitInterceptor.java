package org.bugzkit.api.shared.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
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
  private final Map<String, Cache<String, Bucket>> caches = new ConcurrentHashMap<>();

  public RateLimitInterceptor(
      MessageService messageService, @Value("${rate-limit.enabled:true}") boolean enabled) {
    this.messageService = messageService;
    this.enabled = enabled;
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

    final var key = resolveClientIp(request);
    final var cacheKey =
        handlerMethod.getBeanType().getSimpleName() + ":" + handlerMethod.getMethod().getName();
    final var cache =
        caches.computeIfAbsent(
            cacheKey,
            k ->
                Caffeine.newBuilder()
                    .expireAfterAccess(rateLimit.duration() + 60, TimeUnit.SECONDS)
                    .build());
    final var bucket = cache.get(key, k -> createBucket(rateLimit));
    final var probe = bucket.tryConsumeAndReturnRemaining(1);

    if (probe.isConsumed()) return true;

    writeRateLimitResponse(response, probe);
    return false;
  }

  private Bucket createBucket(RateLimit rateLimit) {
    final var bandwidth =
        Bandwidth.builder()
            .capacity(rateLimit.requests())
            .refillGreedy(rateLimit.requests(), Duration.ofSeconds(rateLimit.duration()))
            .build();
    return Bucket.builder().addLimit(bandwidth).build();
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
