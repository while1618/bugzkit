package org.bugzkit.api.shared.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.bugzkit.api.shared.message.service.impl.MessageServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {
  @Mock private MessageSource messageSource;
  @InjectMocks private MessageServiceImpl messageService;

  @Test
  void getMessage() {
    when(messageSource.getMessage("{auth.unauthorized}", null, LocaleContextHolder.getLocale()))
        .thenReturn("API_ERROR_AUTH_UNAUTHORIZED");
    final var actualMessage = messageService.getMessage("{auth.unauthorized}");
    assertThat(actualMessage).isEqualTo("API_ERROR_AUTH_UNAUTHORIZED");
  }
}
