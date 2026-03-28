package org.bugzkit.api.user.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.bugzkit.api.shared.constants.Regex;

public record UsernameAvailabilityRequest(
    @Schema(example = "john_doe")
        @NotBlank(message = "{user.usernameRequired}")
        @Pattern(regexp = Regex.USERNAME, message = "{user.usernameInvalid}")
        String username) {}
