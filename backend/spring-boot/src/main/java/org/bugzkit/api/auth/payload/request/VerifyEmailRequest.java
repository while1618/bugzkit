package org.bugzkit.api.auth.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(
    @Schema(example = "550e8400-e29b-41d4-a716-446655440000")
        @NotBlank(message = "{auth.tokenRequired}")
        String token) {}
