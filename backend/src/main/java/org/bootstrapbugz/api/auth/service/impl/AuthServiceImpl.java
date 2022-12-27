package org.bootstrapbugz.api.auth.service.impl;

import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import org.bootstrapbugz.api.auth.jwt.service.AccessTokenService;
import org.bootstrapbugz.api.auth.jwt.service.ConfirmRegistrationTokenService;
import org.bootstrapbugz.api.auth.jwt.service.ForgotPasswordTokenService;
import org.bootstrapbugz.api.auth.jwt.service.RefreshTokenService;
import org.bootstrapbugz.api.auth.jwt.util.JwtUtil;
import org.bootstrapbugz.api.auth.payload.dto.RefreshTokenDto;
import org.bootstrapbugz.api.auth.payload.request.ConfirmRegistrationRequest;
import org.bootstrapbugz.api.auth.payload.request.ForgotPasswordRequest;
import org.bootstrapbugz.api.auth.payload.request.RefreshTokenRequest;
import org.bootstrapbugz.api.auth.payload.request.ResendConfirmationEmailRequest;
import org.bootstrapbugz.api.auth.payload.request.ResetPasswordRequest;
import org.bootstrapbugz.api.auth.payload.request.SignUpRequest;
import org.bootstrapbugz.api.auth.service.AuthService;
import org.bootstrapbugz.api.auth.util.AuthUtil;
import org.bootstrapbugz.api.shared.error.exception.ForbiddenException;
import org.bootstrapbugz.api.shared.error.exception.ResourceNotFoundException;
import org.bootstrapbugz.api.shared.message.service.MessageService;
import org.bootstrapbugz.api.user.mapper.UserMapper;
import org.bootstrapbugz.api.user.model.Role;
import org.bootstrapbugz.api.user.model.Role.RoleName;
import org.bootstrapbugz.api.user.model.User;
import org.bootstrapbugz.api.user.payload.dto.UserDto;
import org.bootstrapbugz.api.user.repository.RoleRepository;
import org.bootstrapbugz.api.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final MessageService messageService;
  private final PasswordEncoder bCryptPasswordEncoder;
  private final UserMapper userMapper;
  private final AccessTokenService accessTokenService;
  private final RefreshTokenService refreshTokenService;
  private final ConfirmRegistrationTokenService confirmRegistrationTokenService;
  private final ForgotPasswordTokenService forgotPasswordTokenService;

  public AuthServiceImpl(
      UserRepository userRepository,
      RoleRepository roleRepository,
      MessageService messageService,
      PasswordEncoder bCryptPasswordEncoder,
      UserMapper userMapper,
      AccessTokenService accessTokenService,
      RefreshTokenService refreshTokenService,
      ConfirmRegistrationTokenService confirmRegistrationTokenService,
      ForgotPasswordTokenService forgotPasswordTokenService) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.messageService = messageService;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    this.userMapper = userMapper;
    this.accessTokenService = accessTokenService;
    this.refreshTokenService = refreshTokenService;
    this.confirmRegistrationTokenService = confirmRegistrationTokenService;
    this.forgotPasswordTokenService = forgotPasswordTokenService;
  }

  @Override
  public UserDto signUp(SignUpRequest signUpRequest) {
    final var user = createUser(signUpRequest);
    final var accessToken = confirmRegistrationTokenService.create(user.getId());
    confirmRegistrationTokenService.sendToEmail(user, accessToken);
    return userMapper.userToUserDto(user);
  }

  private User createUser(SignUpRequest signUpRequest) {
    final var user =
        new User()
            .setFirstName(signUpRequest.getFirstName())
            .setLastName(signUpRequest.getLastName())
            .setUsername(signUpRequest.getUsername())
            .setEmail(signUpRequest.getEmail())
            .setPassword(bCryptPasswordEncoder.encode(signUpRequest.getPassword()))
            .setRoles(Collections.singleton(getUserRole()));
    return userRepository.save(user);
  }

  private Role getUserRole() {
    return roleRepository
        .findByName(RoleName.USER)
        .orElseThrow(
            () -> new ResourceNotFoundException(messageService.getMessage("role.notFound")));
  }

  @Override
  public void resendConfirmationEmail(ResendConfirmationEmailRequest request) {
    final var user =
        userRepository
            .findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail())
            .orElseThrow(
                () -> new ResourceNotFoundException(messageService.getMessage("user.notFound")));
    if (user.isActivated())
      throw new ForbiddenException(messageService.getMessage("user.activated"));
    final var accessToken = confirmRegistrationTokenService.create(user.getId());
    confirmRegistrationTokenService.sendToEmail(user, accessToken);
  }

  @Override
  public void confirmRegistration(ConfirmRegistrationRequest confirmRegistrationRequest) {
    final var user =
        userRepository
            .findById(JwtUtil.getUserId(confirmRegistrationRequest.getAccessToken()))
            .orElseThrow(() -> new ForbiddenException(messageService.getMessage("token.invalid")));
    if (user.isActivated())
      throw new ForbiddenException(messageService.getMessage("user.activated"));
    confirmRegistrationTokenService.check(confirmRegistrationRequest.getAccessToken());
    user.setActivated(true);
    userRepository.save(user);
  }

  @Override
  public RefreshTokenDto refreshToken(
      RefreshTokenRequest refreshTokenRequest, HttpServletRequest request) {
    final String oldRefreshToken = JwtUtil.removeBearer(refreshTokenRequest.getRefreshToken());
    refreshTokenService.check(oldRefreshToken);
    final Long userId = JwtUtil.getUserId(oldRefreshToken);
    final var roles = JwtUtil.getRolesDto(oldRefreshToken);
    refreshTokenService.delete(oldRefreshToken);
    final String accessToken = accessTokenService.create(userId, roles);
    final String newRefreshToken =
        refreshTokenService.create(userId, roles, AuthUtil.getUserIpAddress(request));
    return new RefreshTokenDto(JwtUtil.addBearer(accessToken), JwtUtil.addBearer(newRefreshToken));
  }

  @Override
  public void signOut(HttpServletRequest request) {
    refreshTokenService.deleteByUserAndIpAddress(
        AuthUtil.findSignedInUser().getId(), AuthUtil.getUserIpAddress(request));
    accessTokenService.invalidate(
        JwtUtil.removeBearer(AuthUtil.getAccessTokenFromRequest(request)));
  }

  @Override
  public void signOutFromAllDevices() {
    final Long userId = AuthUtil.findSignedInUser().getId();
    refreshTokenService.deleteAllByUser(userId);
    accessTokenService.invalidateAllByUser(userId);
  }

  @Override
  public void forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
    final var user =
        userRepository
            .findByEmail(forgotPasswordRequest.getEmail())
            .orElseThrow(
                () -> new ResourceNotFoundException(messageService.getMessage("user.notFound")));
    var accessToken = forgotPasswordTokenService.create(user.getId());
    forgotPasswordTokenService.sendToEmail(user, accessToken);
  }

  @Override
  public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
    final var user =
        userRepository
            .findById(JwtUtil.getUserId(resetPasswordRequest.getAccessToken()))
            .orElseThrow(() -> new ForbiddenException(messageService.getMessage("token.invalid")));
    forgotPasswordTokenService.check(resetPasswordRequest.getAccessToken());
    user.setPassword(bCryptPasswordEncoder.encode(resetPasswordRequest.getPassword()));
    accessTokenService.invalidateAllByUser(user.getId());
    refreshTokenService.deleteAllByUser(user.getId());
    userRepository.save(user);
  }

  @Override
  public UserDto signedInUser() {
    return AuthUtil.findSignedInUser();
  }

  @Override
  public boolean isUsernameAvailable(String username) {
    return !userRepository.existsByUsername(username);
  }

  @Override
  public boolean isEmailAvailable(String email) {
    return !userRepository.existsByEmail(email);
  }
}