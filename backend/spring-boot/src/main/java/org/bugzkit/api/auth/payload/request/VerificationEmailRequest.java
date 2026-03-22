package org.bugzkit.api.auth.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import org.bugzkit.api.shared.validator.UsernameOrEmail;

public record VerificationEmailRequest(
    @Schema(example = "john_doe")
        @NotBlank(message = "{user.usernameOrEmailRequired}")
        @UsernameOrEmail(message = "{user.usernameOrEmailInvalid}")
        String usernameOrEmail) {}
