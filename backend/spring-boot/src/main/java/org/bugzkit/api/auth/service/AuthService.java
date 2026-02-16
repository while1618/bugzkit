package org.bugzkit.api.auth.service;

import org.bugzkit.api.auth.AuthTokens;
import org.bugzkit.api.auth.payload.request.AuthTokensRequest;
import org.bugzkit.api.auth.payload.request.ForgotPasswordRequest;
import org.bugzkit.api.auth.payload.request.RegisterUserRequest;
import org.bugzkit.api.auth.payload.request.ResetPasswordRequest;
import org.bugzkit.api.auth.payload.request.VerificationEmailRequest;
import org.bugzkit.api.auth.payload.request.VerifyEmailRequest;
import org.bugzkit.api.user.payload.dto.UserDTO;

public interface AuthService {
  UserDTO register(RegisterUserRequest registerUserRequest);

  AuthTokens authenticate(AuthTokensRequest authTokensRequest, String deviceId, String userAgent);

  void deleteTokens(String accessToken, String deviceId);

  void deleteTokensOnAllDevices();

  AuthTokens refreshTokens(String refreshToken, String deviceId, String userAgent);

  void forgotPassword(ForgotPasswordRequest forgotPasswordRequest);

  void resetPassword(ResetPasswordRequest resetPasswordRequest);

  void sendVerificationMail(VerificationEmailRequest verificationEmailRequest);

  void verifyEmail(VerifyEmailRequest verifyEmailRequest);
}
