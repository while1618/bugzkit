package org.bugzkit.api.auth.email;

import java.io.Serial;
import lombok.Getter;
import org.bugzkit.api.user.model.User;
import org.springframework.context.ApplicationEvent;

@Getter
public class OnSendAuthEmail extends ApplicationEvent {
  @Serial private static final long serialVersionUID = 6234594744610595283L;
  private final User user;
  private final String token;
  private final AuthEmailPurpose purpose;

  public OnSendAuthEmail(User user, String token, AuthEmailPurpose purpose) {
    super(user);
    this.user = user;
    this.token = token;
    this.purpose = purpose;
  }
}
