package org.bugzkit.api.shared.logger;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class AspectLogger {
  public AspectLogger() {
    log.info("AspectLogger Initialized");
  }

  @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
  public void controllerLayer() {}

  @Before(value = "controllerLayer()")
  public void logBefore() {
    log.info("Called");
  }

  @AfterReturning(value = "controllerLayer()")
  public void logAfter() {
    log.info("Finished");
  }
}
