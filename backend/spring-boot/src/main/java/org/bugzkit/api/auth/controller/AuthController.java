package org.bugzkit.api.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.bugzkit.api.auth.payload.request.AuthTokensRequest;
import org.bugzkit.api.auth.payload.request.ForgotPasswordRequest;
import org.bugzkit.api.auth.payload.request.RegisterUserRequest;
import org.bugzkit.api.auth.payload.request.ResetPasswordRequest;
import org.bugzkit.api.auth.payload.request.VerificationEmailRequest;
import org.bugzkit.api.auth.payload.request.VerifyEmailRequest;
import org.bugzkit.api.auth.service.AuthService;
import org.bugzkit.api.auth.util.AuthUtil;
import org.bugzkit.api.shared.constants.Path;
import org.bugzkit.api.user.payload.dto.UserDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Path.AUTH)
public class AuthController {
  private final AuthService authService;

  @Value("${jwt.access-token.duration}")
  private int accessTokenDuration;

  @Value("${jwt.refresh-token.duration}")
  private int refreshTokenDuration;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  public ResponseEntity<UserDTO> register(
      @Valid @RequestBody RegisterUserRequest registerUserRequest) {
    return new ResponseEntity<>(authService.register(registerUserRequest), HttpStatus.CREATED);
  }

  @PostMapping("/tokens")
  public ResponseEntity<Void> authenticate(
      @Valid @RequestBody AuthTokensRequest authTokensRequest, HttpServletRequest request) {
    final var ipAddress = AuthUtil.getUserIpAddress(request);
    final var authTokensDTO = authService.authenticate(authTokensRequest, ipAddress);
    final var accessTokenCookie =
        AuthUtil.createCookie("accessToken", authTokensDTO.accessToken(), accessTokenDuration);
    final var refreshTokenCookie =
        AuthUtil.createCookie("refreshToken", authTokensDTO.refreshToken(), refreshTokenDuration);
    return ResponseEntity.noContent()
        .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
        .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
        .build();
  }

  @DeleteMapping("/tokens")
  public ResponseEntity<Void> deleteTokens(HttpServletRequest request) {
    final var accessToken = AuthUtil.getValueFromCookie("accessToken", request);
    final var ipAddress = AuthUtil.getUserIpAddress(request);
    authService.deleteTokens(accessToken, ipAddress);
    final var accessTokenCookie = AuthUtil.createCookie("accessToken", "", 0);
    final var refreshTokenCookie = AuthUtil.createCookie("refreshToken", "", 0);
    return ResponseEntity.noContent()
        .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
        .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
        .build();
  }

  @DeleteMapping("/tokens/devices")
  public ResponseEntity<Void> deleteTokensOnAllDevices() {
    authService.deleteTokensOnAllDevices();
    final var accessTokenCookie = AuthUtil.createCookie("accessToken", "", 0);
    final var refreshTokenCookie = AuthUtil.createCookie("refreshToken", "", 0);
    return ResponseEntity.noContent()
        .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
        .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
        .build();
  }

  @PostMapping("/tokens/refresh")
  public ResponseEntity<Void> refreshTokens(HttpServletRequest request) {
    final var refreshToken = AuthUtil.getValueFromCookie("refreshToken", request);
    final var ipAddress = AuthUtil.getUserIpAddress(request);
    final var authTokensDTO = authService.refreshTokens(refreshToken, ipAddress);
    final var accessTokenCookie =
        AuthUtil.createCookie("accessToken", authTokensDTO.accessToken(), accessTokenDuration);
    final var refreshTokenCookie =
        AuthUtil.createCookie("refreshToken", authTokensDTO.refreshToken(), refreshTokenDuration);
    return ResponseEntity.noContent()
        .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
        .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
        .build();
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
}
