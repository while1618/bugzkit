package org.bugzkit.api.auth.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import org.bugzkit.api.auth.AuthTokens;
import org.bugzkit.api.auth.jwt.service.AccessTokenService;
import org.bugzkit.api.auth.jwt.service.RefreshTokenService;
import org.bugzkit.api.auth.jwt.service.ResetPasswordTokenService;
import org.bugzkit.api.auth.jwt.service.VerificationTokenService;
import org.bugzkit.api.auth.jwt.util.JwtUtil;
import org.bugzkit.api.auth.payload.request.AuthTokensRequest;
import org.bugzkit.api.auth.payload.request.ForgotPasswordRequest;
import org.bugzkit.api.auth.payload.request.RegisterUserRequest;
import org.bugzkit.api.auth.payload.request.ResetPasswordRequest;
import org.bugzkit.api.auth.payload.request.VerificationEmailRequest;
import org.bugzkit.api.auth.payload.request.VerifyEmailRequest;
import org.bugzkit.api.auth.service.AuthService;
import org.bugzkit.api.auth.service.DeviceService;
import org.bugzkit.api.auth.util.AuthUtil;
import org.bugzkit.api.shared.error.exception.BadRequestException;
import org.bugzkit.api.shared.error.exception.ConflictException;
import org.bugzkit.api.shared.error.exception.ResourceNotFoundException;
import org.bugzkit.api.shared.error.exception.UnauthorizedException;
import org.bugzkit.api.user.mapper.UserMapper;
import org.bugzkit.api.user.model.Role.RoleName;
import org.bugzkit.api.user.model.User;
import org.bugzkit.api.user.payload.dto.UserDTO;
import org.bugzkit.api.user.repository.RoleRepository;
import org.bugzkit.api.user.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder bCryptPasswordEncoder;
  private final AuthenticationManager authenticationManager;
  private final AccessTokenService accessTokenService;
  private final RefreshTokenService refreshTokenService;
  private final VerificationTokenService verificationTokenService;
  private final ResetPasswordTokenService resetPasswordTokenService;
  private final DeviceService deviceService;

  public AuthServiceImpl(
      UserRepository userRepository,
      RoleRepository roleRepository,
      PasswordEncoder bCryptPasswordEncoder,
      AuthenticationManager authenticationManager,
      AccessTokenService accessTokenService,
      RefreshTokenService refreshTokenService,
      VerificationTokenService verificationTokenService,
      ResetPasswordTokenService resetPasswordTokenService,
      DeviceService deviceService) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    this.authenticationManager = authenticationManager;
    this.accessTokenService = accessTokenService;
    this.refreshTokenService = refreshTokenService;
    this.verificationTokenService = verificationTokenService;
    this.resetPasswordTokenService = resetPasswordTokenService;
    this.deviceService = deviceService;
  }

  @Override
  @Transactional
  public UserDTO register(RegisterUserRequest registerUserRequest) {
    if (userRepository.existsByUsername(registerUserRequest.username()))
      throw new ConflictException("user.usernameExists");
    if (userRepository.existsByEmail(registerUserRequest.email()))
      throw new ConflictException("user.emailExists");
    final var user = userRepository.save(createUser(registerUserRequest));
    final var token = verificationTokenService.create(user.getId());
    verificationTokenService.sendToEmail(user, token);
    return UserMapper.INSTANCE.userToProfileUserDTO(user);
  }

  @Override
  public AuthTokens authenticate(
      AuthTokensRequest authTokensRequest, String deviceId, String userAgent) {
    final var auth =
        new UsernamePasswordAuthenticationToken(
            authTokensRequest.usernameOrEmail(), authTokensRequest.password(), new ArrayList<>());
    authenticationManager.authenticate(auth);

    final var user =
        userRepository
            .findWithRolesByUsername(auth.getName())
            .orElseThrow(() -> new UnauthorizedException("auth.unauthorized"));
    final var roleDTOs = UserMapper.INSTANCE.rolesToRoleDTOs(user.getRoles());
    final var accessToken = accessTokenService.create(user.getId(), roleDTOs);
    final var refreshToken =
        refreshTokenService
            .findByUserIdAndDeviceId(user.getId(), deviceId)
            .orElse(refreshTokenService.create(user.getId(), roleDTOs, deviceId));
    deviceService.createOrUpdate(user.getId(), deviceId, userAgent);
    return new AuthTokens(accessToken, refreshToken, deviceId);
  }

  private User createUser(RegisterUserRequest registerUserRequest) {
    final var roles =
        roleRepository
            .findByName(RoleName.USER)
            .orElseThrow(() -> new RuntimeException("user.roleNotFound"));
    return User.builder()
        .username(registerUserRequest.username())
        .email(registerUserRequest.email())
        .password(bCryptPasswordEncoder.encode(registerUserRequest.password()))
        .roles(Collections.singleton(roles))
        .build();
  }

  @Override
  @Transactional
  public void deleteTokens(String accessToken, String deviceId) {
    if (!AuthUtil.isSignedIn()) return;
    final var id = AuthUtil.findSignedInUser().getId();
    refreshTokenService.deleteByUserIdAndDeviceId(id, deviceId);
    accessTokenService.invalidate(accessToken);
    deviceService.deleteByUserIdAndDeviceId(id, deviceId);
  }

  @Override
  @Transactional
  public void deleteTokensOnAllDevices() {
    if (!AuthUtil.isSignedIn()) return;
    final var id = AuthUtil.findSignedInUser().getId();
    refreshTokenService.deleteAllByUserId(id);
    accessTokenService.invalidateAllByUserId(id);
    deviceService.deleteAllByUserId(id);
  }

  @Override
  public AuthTokens refreshTokens(String refreshToken, String deviceId, String userAgent) {
    refreshTokenService.check(refreshToken);
    final var userId = JwtUtil.getUserId(refreshToken);
    final var roleDTOs = JwtUtil.getRoleDTOs(refreshToken);
    refreshTokenService.delete(refreshToken);
    final var newAccessToken = accessTokenService.create(userId, roleDTOs);
    final var newRefreshToken = refreshTokenService.create(userId, roleDTOs, deviceId);
    deviceService.createOrUpdate(userId, deviceId, userAgent);
    return new AuthTokens(newAccessToken, newRefreshToken, deviceId);
  }

  @Override
  @Transactional
  public void forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
    final var user =
        userRepository
            .findByEmail(forgotPasswordRequest.email())
            .orElseThrow(() -> new ResourceNotFoundException("user.notFound"));
    final var token = resetPasswordTokenService.create(user.getId());
    resetPasswordTokenService.sendToEmail(user, token);
  }

  @Override
  @Transactional
  public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
    final var user =
        userRepository
            .findById(JwtUtil.getUserId(resetPasswordRequest.token()))
            .orElseThrow(() -> new BadRequestException("auth.tokenInvalid"));
    resetPasswordTokenService.check(resetPasswordRequest.token());
    user.setPassword(bCryptPasswordEncoder.encode(resetPasswordRequest.password()));
    accessTokenService.invalidateAllByUserId(user.getId());
    refreshTokenService.deleteAllByUserId(user.getId());
    userRepository.save(user);
  }

  @Override
  public void sendVerificationMail(VerificationEmailRequest request) {
    final var user =
        userRepository
            .findByUsernameOrEmail(request.usernameOrEmail(), request.usernameOrEmail())
            .orElseThrow(() -> new ResourceNotFoundException("user.notFound"));
    if (Boolean.TRUE.equals(user.getActive())) throw new ConflictException("user.active");
    final var token = verificationTokenService.create(user.getId());
    verificationTokenService.sendToEmail(user, token);
  }

  @Override
  public void verifyEmail(VerifyEmailRequest verifyEmailRequest) {
    final var user =
        userRepository
            .findById(JwtUtil.getUserId(verifyEmailRequest.token()))
            .orElseThrow(() -> new BadRequestException("auth.tokenInvalid"));
    if (Boolean.TRUE.equals(user.getActive())) throw new ConflictException("user.active");
    verificationTokenService.check(verifyEmailRequest.token());
    user.setActive(true);
    userRepository.save(user);
  }
}
