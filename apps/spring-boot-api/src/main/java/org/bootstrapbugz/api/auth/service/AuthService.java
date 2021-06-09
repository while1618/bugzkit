package org.bootstrapbugz.api.auth.service;

import javax.servlet.http.HttpServletRequest;
import org.bootstrapbugz.api.auth.response.RefreshTokenResponse;
import org.bootstrapbugz.api.auth.request.ForgotPasswordRequest;
import org.bootstrapbugz.api.auth.request.RefreshTokenRequest;
import org.bootstrapbugz.api.auth.request.ResendConfirmationEmailRequest;
import org.bootstrapbugz.api.auth.request.ResetPasswordRequest;
import org.bootstrapbugz.api.auth.request.SignUpRequest;
import org.bootstrapbugz.api.user.dto.UserDto;

public interface AuthService {
  RefreshTokenResponse refreshToken(RefreshTokenRequest refreshTokenRequest, HttpServletRequest request);

  UserDto signUp(SignUpRequest signUpRequest);

  void confirmRegistration(String token);

  void resendConfirmationEmail(ResendConfirmationEmailRequest resendConfirmationEmailRequest);

  void forgotPassword(ForgotPasswordRequest forgotPasswordRequest);

  void resetPassword(ResetPasswordRequest resetPasswordRequest);

  void logout(String token);

  void logoutFromAllDevices();
}
