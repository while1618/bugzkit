package org.bugzkit.api.shared.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ErrorMessage {
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private final LocalDateTime timestamp;

  private final int status;
  private final String error;
  private final List<String> codes;

  public ErrorMessage(HttpStatus status) {
    this.timestamp = LocalDateTime.now();
    this.status = status.value();
    this.error = status.getReasonPhrase();
    this.codes = new ArrayList<>();
  }

  public void addCode(String code) {
    this.codes.add(code);
  }
}
