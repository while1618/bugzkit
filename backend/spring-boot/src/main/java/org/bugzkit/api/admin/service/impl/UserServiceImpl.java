package org.bugzkit.api.admin.service.impl;

import java.util.HashSet;
import java.util.Set;
import org.bugzkit.api.admin.payload.request.PatchUserRequest;
import org.bugzkit.api.admin.payload.request.UserRequest;
import org.bugzkit.api.admin.service.UserService;
import org.bugzkit.api.auth.service.AccessTokenService;
import org.bugzkit.api.auth.service.RefreshTokenService;
import org.bugzkit.api.auth.util.AuthUtil;
import org.bugzkit.api.shared.error.exception.BadRequestException;
import org.bugzkit.api.shared.error.exception.ConflictException;
import org.bugzkit.api.shared.error.exception.ResourceNotFoundException;
import org.bugzkit.api.shared.payload.dto.PageableDTO;
import org.bugzkit.api.user.mapper.UserMapper;
import org.bugzkit.api.user.model.Role.RoleName;
import org.bugzkit.api.user.model.User;
import org.bugzkit.api.user.payload.dto.UserDTO;
import org.bugzkit.api.user.repository.RoleRepository;
import org.bugzkit.api.user.repository.UserRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("adminUserService")
@PreAuthorize("hasAuthority('ADMIN')")
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final AccessTokenService accessTokenService;
  private final RefreshTokenService refreshTokenService;
  private final PasswordEncoder bCryptPasswordEncoder;

  public UserServiceImpl(
      UserRepository userRepository,
      RoleRepository roleRepository,
      AccessTokenService accessTokenService,
      RefreshTokenService refreshTokenService,
      PasswordEncoder bCryptPasswordEncoder) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.accessTokenService = accessTokenService;
    this.refreshTokenService = refreshTokenService;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
  }

  @Override
  @Transactional
  public UserDTO create(UserRequest userRequest) {
    if (userRepository.existsByUsername(userRequest.username()))
      throw new ConflictException("user.usernameExists");
    if (userRepository.existsByEmail(userRequest.email()))
      throw new ConflictException("user.emailExists");
    final var user =
        User.builder()
            .username(userRequest.username())
            .email(userRequest.email())
            .password(bCryptPasswordEncoder.encode(userRequest.password()))
            .active(userRequest.active())
            .lock(userRequest.lock())
            .roles(new HashSet<>(roleRepository.findAllByNameIn(userRequest.roleNames())))
            .build();
    return UserMapper.INSTANCE.userToAdminUserDTO(userRepository.save(user));
  }

  /*
   * This method is using two query calls because of HHH000104 warning
   * https://vladmihalcea.com/fix-hibernate-hhh000104-entity-fetch-pagination-warning-message/
   * https://vladmihalcea.com/join-fetch-pagination-spring/
   * */
  @Override
  @Transactional(readOnly = true)
  public PageableDTO<UserDTO> findAll(Pageable pageable) {
    final var userIds = userRepository.findAllUserIds(pageable);
    final var users =
        userRepository.findAllByIdIn(userIds).stream()
            .map(UserMapper.INSTANCE::userToAdminUserDTO)
            .toList();
    final long total = userRepository.count();
    return new PageableDTO<>(users, total);
  }

  @Override
  public UserDTO findById(Long id) {
    return userRepository
        .findWithRolesById(id)
        .map(UserMapper.INSTANCE::userToAdminUserDTO)
        .orElseThrow(() -> new ResourceNotFoundException("user.notFound"));
  }

  @Override
  @Transactional
  public UserDTO update(Long id, UserRequest userRequest) {
    final var user =
        userRepository
            .findWithRolesById(id)
            .orElseThrow(() -> new ResourceNotFoundException("user.notFound"));

    setUsername(user, userRequest.username());
    setEmail(user, userRequest.email());
    setPassword(user, userRequest.password());

    if (!isSelf(id)) {
      setActive(user, userRequest.active());
      setLock(user, userRequest.lock());
      setRoles(user, userRequest.roleNames());
    }

    return UserMapper.INSTANCE.userToAdminUserDTO(userRepository.save(user));
  }

  @Override
  @Transactional
  public UserDTO patch(Long id, PatchUserRequest patchUserRequest) {
    final var user =
        userRepository
            .findWithRolesById(id)
            .orElseThrow(() -> new ResourceNotFoundException("user.notFound"));

    if (patchUserRequest.username() != null) setUsername(user, patchUserRequest.username());
    if (patchUserRequest.email() != null) setEmail(user, patchUserRequest.email());
    if (patchUserRequest.password() != null) setPassword(user, patchUserRequest.password());

    if (!isSelf(id)) {
      if (patchUserRequest.active() != null) setActive(user, patchUserRequest.active());
      if (patchUserRequest.lock() != null) setLock(user, patchUserRequest.lock());
      if (patchUserRequest.roleNames() != null) setRoles(user, patchUserRequest.roleNames());
    }

    return UserMapper.INSTANCE.userToAdminUserDTO(userRepository.save(user));
  }

  private boolean isSelf(Long id) {
    return AuthUtil.findSignedInUser().getId().equals(id);
  }

  private void deleteAuthTokens(Long userId) {
    accessTokenService.invalidateAllByUserId(userId);
    refreshTokenService.deleteAllByUserId(userId);
  }

  private void setUsername(User user, String username) {
    if (username.equals(user.getUsername())) return;
    if (userRepository.existsByUsername(username))
      throw new ConflictException("user.usernameExists");

    user.setUsername(username);
  }

  private void setEmail(User user, String email) {
    if (email.equals(user.getEmail())) return;
    if (userRepository.existsByEmail(email)) throw new ConflictException("user.emailExists");

    user.setEmail(email);
  }

  private void setPassword(User user, String password) {
    if (user.getPassword() != null && bCryptPasswordEncoder.matches(password, user.getPassword()))
      return;

    user.setPassword(bCryptPasswordEncoder.encode(password));
    if (user.getId() != null) deleteAuthTokens(user.getId());
  }

  private void setActive(User user, Boolean active) {
    if (user.getActive().equals(active)) return;

    user.setActive(active);
    if (Boolean.FALSE.equals(active)) deleteAuthTokens(user.getId());
  }

  private void setLock(User user, Boolean lock) {
    if (user.getLock().equals(lock)) return;

    user.setLock(lock);
    if (Boolean.TRUE.equals(lock)) deleteAuthTokens(user.getId());
  }

  private void setRoles(User user, Set<RoleName> roleNames) {
    if (roleNames.isEmpty()) throw new BadRequestException("user.rolesEmpty");

    final var roles = new HashSet<>(roleRepository.findAllByNameIn(roleNames));
    if (user.getRoles().equals(roles)) return;

    user.setRoles(roles);
    deleteAuthTokens(user.getId());
  }

  @Override
  public void delete(Long id) {
    deleteAuthTokens(id);
    userRepository.deleteById(id);
  }
}
