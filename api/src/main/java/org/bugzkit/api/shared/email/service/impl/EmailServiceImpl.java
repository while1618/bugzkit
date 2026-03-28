package org.bugzkit.api.shared.email.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import org.bugzkit.api.shared.email.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {
  private final JavaMailSender mailSender;

  @Value("${app.name}")
  private String appName;

  @Value("${spring.mail.properties.sender}")
  private String sender;

  public EmailServiceImpl(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  @Override
  public void sendHtmlEmail(String to, String subject, String body)
      throws MessagingException, UnsupportedEncodingException {
    final var mimeMessage = mailSender.createMimeMessage();
    final var helper = new MimeMessageHelper(mimeMessage, StandardCharsets.UTF_8.name());
    helper.setFrom(new InternetAddress(sender, appName));
    helper.setTo(to);
    helper.setSubject(subject);
    helper.setText(body, true);
    mailSender.send(mimeMessage);
  }
}
