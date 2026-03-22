package org.bugzkit.api.user.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.bugzkit.api.shared.constants.Regex;
import org.bugzkit.api.shared.validator.FieldMatch;

@FieldMatch(
    first = "newPassword",
    second = "confirmNewPassword",
    message = "{user.passwordsDoNotMatch}")
public record ChangePasswordRequest(
    @Schema(example = "OldSecret123!")
        @NotBlank(message = "{user.passwordRequired}")
        @Pattern(regexp = Regex.PASSWORD, message = "{user.passwordInvalid}")
        String currentPassword,
    @Schema(example = "NewSecret123!")
        @NotBlank(message = "{user.passwordRequired}")
        @Pattern(regexp = Regex.PASSWORD, message = "{user.passwordInvalid}")
        String newPassword,
    @Schema(example = "NewSecret123!")
        @NotBlank(message = "{user.passwordRequired}")
        @Pattern(regexp = Regex.PASSWORD, message = "{user.passwordInvalid}")
        String confirmNewPassword) {}
