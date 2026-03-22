package org.bugzkit.api.user.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.bugzkit.api.auth.service.AccessTokenService;
import org.bugzkit.api.auth.service.DeviceService;
import org.bugzkit.api.auth.service.RefreshTokenService;
import org.bugzkit.api.auth.service.VerificationTokenService;
import org.bugzkit.api.auth.util.AuthUtil;
import org.bugzkit.api.shared.error.exception.BadRequestException;
import org.bugzkit.api.shared.error.exception.ConflictException;
import org.bugzkit.api.shared.error.exception.UnauthorizedException;
import org.bugzkit.api.user.mapper.UserMapper;
import org.bugzkit.api.user.model.User;
import org.bugzkit.api.user.payload.dto.UserDTO;
import org.bugzkit.api.user.payload.request.ChangePasswordRequest;
import org.bugzkit.api.user.payload.request.PatchProfileRequest;
import org.bugzkit.api.user.repository.UserRepository;
import org.bugzkit.api.user.service.ProfileService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ProfileServiceImpl implements ProfileService {
  private final UserRepository userRepository;
  private final PasswordEncoder bCryptPasswordEncoder;
  private final AccessTokenService accessTokenService;
  private final RefreshTokenService refreshTokenService;
  private final VerificationTokenService verificationTokenService;
  private final DeviceService deviceService;
  private final UserMapper userMapper;

  public ProfileServiceImpl(
      UserRepository userRepository,
      PasswordEncoder bCryptPasswordEncoder,
      AccessTokenService accessTokenService,
      RefreshTokenService refreshTokenService,
      VerificationTokenService verificationTokenService,
      DeviceService deviceService,
      UserMapper userMapper) {
    this.userRepository = userRepository;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    this.accessTokenService = accessTokenService;
    this.refreshTokenService = refreshTokenService;
    this.verificationTokenService = verificationTokenService;
    this.deviceService = deviceService;
    this.userMapper = userMapper;
  }

  @Override
  public UserDTO find() {
    return userMapper.userPrincipalToProfileUserDTO(AuthUtil.findSignedInUser());
  }

  @Override
  @Transactional
  public UserDTO patch(PatchProfileRequest patchProfileRequest) {
    final var userId = AuthUtil.findSignedInUser().getId();
    final var user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> {
                  log.error(
                      "Profile patch failed: token was valid but no user found for userId '{}' — possible data inconsistency",
                      userId);
                  return new UnauthorizedException("auth.tokenInvalid");
                });

    if (patchProfileRequest.username() != null) setUsername(user, patchProfileRequest.username());
    if (patchProfileRequest.email() != null) setEmail(user, patchProfileRequest.email());

    final var updated = userRepository.save(user);
    log.info("Profile updated for user '{}'", userId);
    return userMapper.userToProfileUserDTO(updated);
  }

  private void deleteAuthTokens(Long userId) {
    accessTokenService.invalidateAllByUserId(userId);
    refreshTokenService.deleteAllByUserId(userId);
  }

  private void setUsername(User user, String username) {
    if (user.getUsername() != null && user.getUsername().equals(username)) return;
    if (userRepository.existsByUsername(username)) {
      log.warn("Profile update failed: username '{}' already exists", username);
      throw new ConflictException("user.usernameExists");
    }
    user.setUsername(username);
  }

  private void setEmail(User user, String email) {
    if (user.getEmail().equals(email)) return;
    if (userRepository.existsByEmail(email)) {
      log.warn("Profile update failed: email '{}' already exists", email);
      throw new ConflictException("user.emailExists");
    }
    user.setEmail(email);
    user.setActive(false);
    deleteAuthTokens(user.getId());
    final var token = verificationTokenService.create(user.getId());
    verificationTokenService.sendToEmail(user, token);
    log.info(
        "Email changed for user '{}', tokens invalidated, verification email sent", user.getId());
  }

  @Override
  @Transactional
  public void delete() {
    final var userId = AuthUtil.findSignedInUser().getId();
    deviceService.deleteAllByUserId(userId);
    deleteAuthTokens(userId);
    userRepository.deleteById(userId);
    log.info("Account deleted for user '{}'", userId);
  }

  @Override
  @Transactional
  public void changePassword(ChangePasswordRequest changePasswordRequest) {
    final var userId = AuthUtil.findSignedInUser().getId();
    final var user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> {
                  log.error(
                      "Password change failed: token was valid but no user found for userId '{}' — possible data inconsistency",
                      userId);
                  return new UnauthorizedException("auth.tokenInvalid");
                });
    if (!bCryptPasswordEncoder.matches(
        changePasswordRequest.currentPassword(), user.getPassword())) {
      log.warn("Password change failed for user '{}': current password is wrong", userId);
      throw new BadRequestException("user.currentPasswordWrong");
    }
    user.setPassword(bCryptPasswordEncoder.encode(changePasswordRequest.newPassword()));
    deleteAuthTokens(userId);
    userRepository.save(user);
    log.info("Password changed for user '{}', all tokens invalidated", userId);
  }
}
