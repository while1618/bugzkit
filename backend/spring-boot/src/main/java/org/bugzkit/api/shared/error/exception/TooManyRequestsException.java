package org.bugzkit.api.shared.error.exception;

import java.io.Serial;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class TooManyRequestsException extends RuntimeException {
  @Serial private static final long serialVersionUID = -2937654540916338509L;
  private final HttpStatus status;

  public TooManyRequestsException(String message) {
    super(message);
    this.status = HttpStatus.TOO_MANY_REQUESTS;
  }
}
