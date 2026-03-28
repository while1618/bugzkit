package org.bugzkit.api.shared.email.service;

import jakarta.mail.MessagingException;
import java.io.UnsupportedEncodingException;

public interface EmailService {
  void sendHtmlEmail(String to, String subject, String body)
      throws MessagingException, UnsupportedEncodingException;
}
