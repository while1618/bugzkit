package org.bugzkit.api.auth.email;

import jakarta.mail.MessagingException;
import java.io.IOException;
import org.bugzkit.api.shared.email.service.EmailService;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class OnSendAuthEmailListener implements ApplicationListener<OnSendAuthEmail> {
  private final EmailService emailService;
  private final Environment environment;

  public OnSendAuthEmailListener(EmailService emailService, Environment environment) {
    this.emailService = emailService;
    this.environment = environment;
  }

  @Override
  public void onApplicationEvent(OnSendAuthEmail event) {
    try {
      new AuthEmailSupplier()
          .supplyEmail(event.getPurpose())
          .sendEmail(emailService, environment, event.getUser(), event.getToken());
    } catch (IOException | MessagingException e) {
      throw new RuntimeException(e);
    }
  }
}
