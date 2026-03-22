package org.bugzkit.api.user.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import org.bugzkit.api.shared.constants.Regex;

@Builder
public record PatchProfileRequest(
    @Schema(example = "john_doe")
        @Pattern(regexp = Regex.USERNAME, message = "{user.usernameInvalid}")
        String username,
    @Schema(example = "john@example.com")
        @Email(message = "{user.emailInvalid}", regexp = Regex.EMAIL)
        String email) {}
