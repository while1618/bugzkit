package org.bugzkit.api.shared.error.exception;

import java.io.Serial;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BadRequestException extends RuntimeException {
  @Serial private static final long serialVersionUID = -6237654540916338509L;
  private final HttpStatus status;

  public BadRequestException(String message) {
    super(message);
    this.status = HttpStatus.BAD_REQUEST;
  }
}
