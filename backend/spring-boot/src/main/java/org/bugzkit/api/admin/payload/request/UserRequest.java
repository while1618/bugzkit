package org.bugzkit.api.admin.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.Set;
import lombok.Builder;
import org.bugzkit.api.shared.constants.Regex;
import org.bugzkit.api.shared.validator.FieldMatch;
import org.bugzkit.api.user.model.Role.RoleName;

@Builder
@FieldMatch(first = "password", second = "confirmPassword", message = "{user.passwordsDoNotMatch}")
public record UserRequest(
    @Schema(example = "john_doe")
        @NotBlank(message = "{user.usernameRequired}")
        @Pattern(regexp = Regex.USERNAME, message = "{user.usernameInvalid}")
        String username,
    @Schema(example = "john@example.com")
        @NotBlank(message = "{user.emailRequired}")
        @Email(message = "{user.emailInvalid}", regexp = Regex.EMAIL)
        String email,
    @Schema(example = "Secret123!")
        @NotBlank(message = "{user.passwordRequired}")
        @Pattern(regexp = Regex.PASSWORD, message = "{user.passwordInvalid}")
        String password,
    @Schema(example = "Secret123!")
        @NotBlank(message = "{user.passwordRequired}")
        @Pattern(regexp = Regex.PASSWORD, message = "{user.passwordInvalid}")
        String confirmPassword,
    @Schema(example = "true") @NotNull(message = "{user.activeRequired}") Boolean active,
    @Schema(example = "false") @NotNull(message = "{user.lockRequired}") Boolean lock,
    @NotEmpty(message = "{user.rolesEmpty}") Set<RoleName> roleNames) {}
