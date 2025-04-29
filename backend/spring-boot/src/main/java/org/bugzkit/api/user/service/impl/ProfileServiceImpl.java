package org.bugzkit.api.user.service.impl;

import org.bugzkit.api.auth.jwt.service.AccessTokenService;
import org.bugzkit.api.auth.jwt.service.RefreshTokenService;
import org.bugzkit.api.auth.jwt.service.VerificationTokenService;
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

@Service
public class ProfileServiceImpl implements ProfileService {
  private final UserRepository userRepository;
  private final PasswordEncoder bCryptPasswordEncoder;
  private final AccessTokenService accessTokenService;
  private final RefreshTokenService refreshTokenService;
  private final VerificationTokenService verificationTokenService;

  public ProfileServiceImpl(
      UserRepository userRepository,
      PasswordEncoder bCryptPasswordEncoder,
      AccessTokenService accessTokenService,
      RefreshTokenService refreshTokenService,
      VerificationTokenService verificationTokenService) {
    this.userRepository = userRepository;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    this.accessTokenService = accessTokenService;
    this.refreshTokenService = refreshTokenService;
    this.verificationTokenService = verificationTokenService;
  }

  @Override
  public UserDTO find() {
    return UserMapper.INSTANCE.userPrincipalToProfileUserDTO(AuthUtil.findSignedInUser());
  }

  @Override
  @Transactional
  public UserDTO patch(PatchProfileRequest patchProfileRequest) {
    final var userId = AuthUtil.findSignedInUser().getId();
    final var user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UnauthorizedException("auth.tokenInvalid"));

    if (patchProfileRequest.username() != null) setUsername(user, patchProfileRequest.username());
    if (patchProfileRequest.email() != null) setEmail(user, patchProfileRequest.email());

    return UserMapper.INSTANCE.userToProfileUserDTO(userRepository.save(user));
  }

  private void deleteAuthTokens(Long userId) {
    accessTokenService.invalidateAllByUserId(userId);
    refreshTokenService.deleteAllByUserId(userId);
  }

  private void setUsername(User user, String username) {
    if (user.getUsername() != null && user.getUsername().equals(username)) return;
    if (userRepository.existsByUsername(username))
      throw new ConflictException("user.usernameExists");

    user.setUsername(username);
  }

  private void setEmail(User user, String email) {
    if (user.getEmail().equals(email)) return;
    if (userRepository.existsByEmail(email)) throw new ConflictException("user.emailExists");

    user.setEmail(email);
    user.setActive(false);
    deleteAuthTokens(user.getId());
    final var token = verificationTokenService.create(user.getId());
    verificationTokenService.sendToEmail(user, token);
  }

  @Override
  public void delete() {
    final var userId = AuthUtil.findSignedInUser().getId();
    deleteAuthTokens(userId);
    userRepository.deleteById(userId);
  }

  @Override
  @Transactional
  public void changePassword(ChangePasswordRequest changePasswordRequest) {
    final var userId = AuthUtil.findSignedInUser().getId();
    final var user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UnauthorizedException("auth.tokenInvalid"));
    if (!bCryptPasswordEncoder.matches(changePasswordRequest.currentPassword(), user.getPassword()))
      throw new BadRequestException("user.currentPasswordWrong");
    user.setPassword(bCryptPasswordEncoder.encode(changePasswordRequest.newPassword()));
    deleteAuthTokens(userId);
    userRepository.save(user);
  }
}
