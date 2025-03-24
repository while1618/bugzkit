package org.bugzkit.api.shared.config;

import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {
  @Value("${spring.mail.host}")
  private String host;

  @Value("${spring.mail.port}")
  private String port;

  @Value("${spring.mail.username}")
  private String username;

  @Value("${spring.mail.password}")
  private String password;

  @Bean
  public JavaMailSender getJavaMailSender() {
    final var mailSender = createMailSender();
    setProperties(mailSender);
    return mailSender;
  }

  private JavaMailSenderImpl createMailSender() {
    final var mailSender = new JavaMailSenderImpl();
    mailSender.setHost(host);
    mailSender.setPort(Integer.parseInt(Objects.requireNonNull(port)));
    mailSender.setUsername(username);
    mailSender.setPassword(password);
    return mailSender;
  }

  private void setProperties(JavaMailSenderImpl mailSender) {
    final var props = mailSender.getJavaMailProperties();
    props.put("mail.transport.protocol", "smtp");
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");
  }
}
