package org.bugzkit.api.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.bugzkit.api.auth.AuthTokens;
import org.bugzkit.api.auth.payload.dto.DeviceDTO;
import org.bugzkit.api.auth.payload.request.AuthTokensRequest;
import org.bugzkit.api.auth.payload.request.ForgotPasswordRequest;
import org.bugzkit.api.auth.payload.request.RegisterUserRequest;
import org.bugzkit.api.auth.payload.request.ResetPasswordRequest;
import org.bugzkit.api.auth.payload.request.VerificationEmailRequest;
import org.bugzkit.api.auth.payload.request.VerifyEmailRequest;
import org.bugzkit.api.auth.service.AuthService;
import org.bugzkit.api.auth.service.DeviceService;
import org.bugzkit.api.auth.util.AuthUtil;
import org.bugzkit.api.auth.util.JwtUtil;
import org.bugzkit.api.shared.constants.Path;
import org.bugzkit.api.shared.error.ErrorMessage;
import org.bugzkit.api.shared.interceptor.RateLimit;
import org.bugzkit.api.user.payload.dto.UserDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "auth")
@RestController
@RequestMapping(Path.AUTH)
public class AuthController {
  private final AuthService authService;
  private final DeviceService deviceService;

  @Value("${domain.name}")
  private String domain;

  @Value("${jwt.access-token.duration}")
  private int accessTokenDuration;

  @Value("${jwt.refresh-token.duration}")
  private int refreshTokenDuration;

  public AuthController(AuthService authService, DeviceService deviceService) {
    this.authService = authService;
    this.deviceService = deviceService;
  }

  @Operation(summary = "Register a new user account")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Registered"),
    @ApiResponse(
        responseCode = "400",
        description = "Validation error",
        content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
    @ApiResponse(
        responseCode = "409",
        description = "Username or email already exists",
        content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
    @ApiResponse(responseCode = "429", description = "Too many requests", content = @Content)
  })
  @RateLimit(requests = 5, duration = 60)
  @PostMapping("/register")
  public ResponseEntity<UserDTO> register(
      @Valid @RequestBody RegisterUserRequest registerUserRequest) {
    return new ResponseEntity<>(authService.register(registerUserRequest), HttpStatus.CREATED);
  }

  @Operation(summary = "Sign in and receive access and refresh token cookies")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Authenticated — cookies set"),
    @ApiResponse(
        responseCode = "400",
        description = "Validation error",
        content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Invalid credentials or account inactive/locked",
        content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
    @ApiResponse(responseCode = "429", description = "Too many requests", content = @Content)
  })
  @RateLimit(requests = 10, duration = 60)
  @PostMapping("/tokens")
  public ResponseEntity<Void> authenticate(
      @Valid @RequestBody AuthTokensRequest authTokensRequest, HttpServletRequest request) {
    final var deviceId = AuthUtil.generateDeviceId();
    final var userAgent = request.getHeader("User-Agent");
    final var authTokens = authService.authenticate(authTokensRequest, deviceId, userAgent);
    return setAuthCookies(authTokens);
  }

  @Operation(summary = "Sign out and clear auth cookies for the current device")
  @ApiResponse(responseCode = "204", description = "Signed out — cookies cleared")
  @DeleteMapping("/tokens")
  public ResponseEntity<Void> deleteTokens(HttpServletRequest request) {
    final var accessToken = AuthUtil.getValueFromCookie("accessToken", request);
    authService.deleteTokens(accessToken);
    return removeAuthCookies();
  }

  @Operation(summary = "List all active devices for the current user")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized",
        content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  })
  @SecurityRequirement(name = "cookieAuth")
  @GetMapping("/tokens/devices")
  public ResponseEntity<List<DeviceDTO>> findAllDevices(HttpServletRequest request) {
    final var accessToken = AuthUtil.getValueFromCookie("accessToken", request);
    final var deviceId = JwtUtil.getDeviceId(accessToken);
    return ResponseEntity.ok(deviceService.findAll(deviceId));
  }

  @Operation(summary = "Sign out from all devices and clear auth cookies")
  @ApiResponse(responseCode = "204", description = "Signed out from all devices")
  @DeleteMapping("/tokens/devices")
  public ResponseEntity<Void> deleteTokensOnAllDevices() {
    authService.deleteTokensOnAllDevices();
    return removeAuthCookies();
  }

  @Operation(summary = "Revoke a specific device and invalidate all access tokens")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Device revoked"),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized",
        content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  })
  @SecurityRequirement(name = "cookieAuth")
  @DeleteMapping("/tokens/devices/{deviceId}")
  public ResponseEntity<Void> revokeDevice(@PathVariable String deviceId) {
    deviceService.revoke(deviceId);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Refresh access and refresh token cookies using the refresh token")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Tokens refreshed — cookies updated"),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid or expired refresh token",
        content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
    @ApiResponse(responseCode = "429", description = "Too many requests", content = @Content)
  })
  @RateLimit(requests = 10, duration = 60)
  @PostMapping("/tokens/refresh")
  public ResponseEntity<Void> refreshTokens(HttpServletRequest request) {
    final var refreshToken = AuthUtil.getValueFromCookie("refreshToken", request);
    final var userAgent = request.getHeader("User-Agent");
    final var authTokens = authService.refreshTokens(refreshToken, userAgent);
    return setAuthCookies(authTokens);
  }

  @Operation(summary = "Send a password reset email")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Email sent if account exists"),
    @ApiResponse(
        responseCode = "400",
        description = "Validation error",
        content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
    @ApiResponse(responseCode = "429", description = "Too many requests", content = @Content)
  })
  @RateLimit(requests = 3, duration = 60)
  @PostMapping("/password/forgot")
  public ResponseEntity<Void> forgotPassword(
      @Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
    authService.forgotPassword(forgotPasswordRequest);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Reset password using a token from the reset email")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Password reset"),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid or expired token, or validation error",
        content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
    @ApiResponse(responseCode = "429", description = "Too many requests", content = @Content)
  })
  @RateLimit(requests = 5, duration = 60)
  @PostMapping("/password/reset")
  public ResponseEntity<Void> resetPassword(
      @Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
    authService.resetPassword(resetPasswordRequest);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Resend the email verification link")
  @ApiResponses({
    @ApiResponse(
        responseCode = "204",
        description = "Email sent if account exists and is inactive"),
    @ApiResponse(
        responseCode = "400",
        description = "Validation error",
        content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
    @ApiResponse(responseCode = "429", description = "Too many requests", content = @Content)
  })
  @RateLimit(requests = 3, duration = 60)
  @PostMapping("/verification-email")
  public ResponseEntity<Void> sendVerificationMail(
      @Valid @RequestBody VerificationEmailRequest verificationEmailRequest) {
    authService.sendVerificationMail(verificationEmailRequest);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Verify email address using a token from the verification email")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Email verified"),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid or expired token",
        content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
    @ApiResponse(responseCode = "429", description = "Too many requests", content = @Content)
  })
  @RateLimit(requests = 5, duration = 60)
  @PostMapping("/verify-email")
  public ResponseEntity<Void> verifyEmail(
      @Valid @RequestBody VerifyEmailRequest verifyEmailRequest) {
    authService.verifyEmail(verifyEmailRequest);
    return ResponseEntity.noContent().build();
  }

  private ResponseEntity<Void> setAuthCookies(AuthTokens authTokens) {
    final var accessTokenCookie =
        AuthUtil.createCookie("accessToken", authTokens.accessToken(), domain, accessTokenDuration);
    final var refreshTokenCookie =
        AuthUtil.createCookie(
            "refreshToken", authTokens.refreshToken(), domain, refreshTokenDuration);
    return ResponseEntity.noContent()
        .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
        .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
        .build();
  }

  private ResponseEntity<Void> removeAuthCookies() {
    final var accessTokenCookie = AuthUtil.createCookie("accessToken", "", domain, 0);
    final var refreshTokenCookie = AuthUtil.createCookie("refreshToken", "", domain, 0);
    return ResponseEntity.noContent()
        .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
        .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
        .build();
  }
}
