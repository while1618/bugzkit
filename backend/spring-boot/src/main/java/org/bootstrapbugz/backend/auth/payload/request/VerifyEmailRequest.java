package org.bootstrapbugz.backend.auth.payload.request;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(@NotBlank(message = "{auth.tokenRequired}") String token) {}