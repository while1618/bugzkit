package org.bugzkit.api.shared.error.exception;

import java.io.Serial;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class UnauthorizedException extends RuntimeException {
  @Serial private static final long serialVersionUID = -8525346226722308705L;
  private final HttpStatus status;

  public UnauthorizedException(String message) {
    super(message);
    this.status = HttpStatus.UNAUTHORIZED;
  }
}
