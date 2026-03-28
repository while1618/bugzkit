package org.bugzkit.api.shared.error.exception;

import java.io.Serial;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ConflictException extends RuntimeException {
  @Serial private static final long serialVersionUID = 8841655774286844538L;
  private final HttpStatus status;

  public ConflictException(String message) {
    super(message);
    this.status = HttpStatus.CONFLICT;
  }
}
