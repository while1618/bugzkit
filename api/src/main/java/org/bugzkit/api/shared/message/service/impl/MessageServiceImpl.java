package org.bugzkit.api.shared.message.service.impl;

import org.bugzkit.api.shared.message.service.MessageService;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl implements MessageService {
  private final MessageSource messageSource;

  public MessageServiceImpl(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @Override
  public String getMessage(String code) {
    return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
  }
}
