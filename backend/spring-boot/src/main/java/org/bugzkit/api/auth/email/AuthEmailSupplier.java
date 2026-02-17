package org.bugzkit.api.auth.email;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public class AuthEmailSupplier {
  private static final Map<AuthEmailPurpose, Supplier<AuthEmail>> emailType =
      new EnumMap<>(AuthEmailPurpose.class);

  static {
    emailType.put(AuthEmailPurpose.VERIFY_EMAIL, VerificationAuthEmail::new);
    emailType.put(AuthEmailPurpose.RESET_PASSWORD, ResetPasswordAuthEmail::new);
  }

  public AuthEmail supplyEmail(AuthEmailPurpose purpose) {
    final var emailSupplier = emailType.get(purpose);
    if (emailSupplier == null) throw new IllegalArgumentException("Invalid email type: " + purpose);
    return emailSupplier.get();
  }
}
