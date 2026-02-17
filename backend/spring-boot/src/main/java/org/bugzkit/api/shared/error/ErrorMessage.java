package org.bugzkit.api.shared.error;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ErrorMessage {
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

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("{\"timestamp\":\"");
    sb.append(timestamp.format(DateTimeFormatter.ISO_DATE_TIME));
    sb.append("\",\"status\":");
    sb.append(status);
    sb.append(",\"error\":\"");
    sb.append(error);
    sb.append("\",\"codes\":[");
    for (int i = 0; i < codes.size(); i++) {
      if (i > 0) sb.append(",");
      sb.append("\"").append(codes.get(i)).append("\"");
    }
    sb.append("]}");
    return sb.toString();
  }
}
