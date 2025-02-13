package org.bugzkit.api.user.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.bugzkit.api.shared.constants.Regex;

public record EmailAvailabilityRequest(
    @NotBlank(message = "{user.emailRequired}")
        @Email(message = "{user.emailInvalid}", regexp = Regex.EMAIL)
        String email) {}
