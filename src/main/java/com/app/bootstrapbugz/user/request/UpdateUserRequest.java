package com.app.bootstrapbugz.user.request;

import com.app.bootstrapbugz.shared.constants.Regex;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequest {
  @Pattern(regexp = Regex.FIRST_AND_LAST_NAME, message = "{firstName.invalid}")
  private String firstName;

  @Pattern(regexp = Regex.FIRST_AND_LAST_NAME, message = "{lastName.invalid}")
  private String lastName;

  @Pattern(regexp = Regex.USERNAME, message = "{username.invalid}")
  private String username;

  @NotEmpty(message = "{email.invalid}")
  @Email(message = "{email.invalid}")
  private String email;
}
