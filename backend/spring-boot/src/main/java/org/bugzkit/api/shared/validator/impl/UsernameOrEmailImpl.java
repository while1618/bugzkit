package org.bugzkit.api.shared.validator.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;
import org.bugzkit.api.shared.constants.Regex;
import org.bugzkit.api.shared.validator.UsernameOrEmail;

public class UsernameOrEmailImpl implements ConstraintValidator<UsernameOrEmail, String> {
  private static final Pattern USERNAME_PATTERN = Pattern.compile(Regex.USERNAME);
  private static final Pattern EMAIL_PATTERN = Pattern.compile(Regex.EMAIL);

  public boolean isValid(String usernameOrEmail, ConstraintValidatorContext context) {
    return usernameOrEmail != null
        && !usernameOrEmail.isEmpty()
        && (USERNAME_PATTERN.matcher(usernameOrEmail).matches()
            || EMAIL_PATTERN.matcher(usernameOrEmail).matches());
  }
}
