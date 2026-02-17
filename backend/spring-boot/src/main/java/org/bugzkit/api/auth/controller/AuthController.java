package org.bugzkit.api.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.bugzkit.api.auth.AuthTokens;
import org.bugzkit.api.auth.jwt.util.JwtUtil;
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
import org.bugzkit.api.shared.constants.Path;
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

  @PostMapping("/register")
  public ResponseEntity<UserDTO> register(
      @Valid @RequestBody RegisterUserRequest registerUserRequest) {
    return new ResponseEntity<>(authService.register(registerUserRequest), HttpStatus.CREATED);
  }

  @PostMapping("/tokens")
  public ResponseEntity<Void> authenticate(
      @Valid @RequestBody AuthTokensRequest authTokensRequest, HttpServletRequest request) {
    final var deviceId = AuthUtil.generateDeviceId();
    final var userAgent = request.getHeader("User-Agent");
    final var authTokens = authService.authenticate(authTokensRequest, deviceId, userAgent);
    return setAuthCookies(authTokens);
  }

  @DeleteMapping("/tokens")
  public ResponseEntity<Void> deleteTokens(HttpServletRequest request) {
    final var accessToken = AuthUtil.getValueFromCookie("accessToken", request);
    authService.deleteTokens(accessToken);
    return removeAuthCookies();
  }

  @GetMapping("/tokens/devices")
  public ResponseEntity<List<DeviceDTO>> findAllDevices(HttpServletRequest request) {
    final var accessToken = AuthUtil.getValueFromCookie("accessToken", request);
    final var deviceId = JwtUtil.getDeviceId(accessToken);
    return ResponseEntity.ok(deviceService.findAll(deviceId));
  }

  @DeleteMapping("/tokens/devices")
  public ResponseEntity<Void> deleteTokensOnAllDevices() {
    authService.deleteTokensOnAllDevices();
    return removeAuthCookies();
  }

  @DeleteMapping("/tokens/devices/{deviceId}")
  public ResponseEntity<Void> revokeDevice(@PathVariable String deviceId) {
    deviceService.revoke(deviceId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/tokens/refresh")
  public ResponseEntity<Void> refreshTokens(HttpServletRequest request) {
    final var refreshToken = AuthUtil.getValueFromCookie("refreshToken", request);
    final var userAgent = request.getHeader("User-Agent");
    final var authTokens = authService.refreshTokens(refreshToken, userAgent);
    return setAuthCookies(authTokens);
  }

  @PostMapping("/password/forgot")
  public ResponseEntity<Void> forgotPassword(
      @Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
    authService.forgotPassword(forgotPasswordRequest);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/password/reset")
  public ResponseEntity<Void> resetPassword(
      @Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
    authService.resetPassword(resetPasswordRequest);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/verification-email")
  public ResponseEntity<Void> sendVerificationMail(
      @Valid @RequestBody VerificationEmailRequest verificationEmailRequest) {
    authService.sendVerificationMail(verificationEmailRequest);
    return ResponseEntity.noContent().build();
  }

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
