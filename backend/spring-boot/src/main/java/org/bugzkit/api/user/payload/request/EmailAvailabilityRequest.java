package org.bugzkit.api.user.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.bugzkit.api.shared.constants.Regex;

public record EmailAvailabilityRequest(
    @Schema(example = "john@example.com")
        @NotBlank(message = "{user.emailRequired}")
        @Email(message = "{user.emailInvalid}", regexp = Regex.EMAIL)
        String email) {}
