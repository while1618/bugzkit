package org.bugzkit.api.user.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.bugzkit.api.shared.error.exception.ResourceNotFoundException;
import org.bugzkit.api.shared.payload.dto.AvailabilityDTO;
import org.bugzkit.api.shared.payload.dto.PageableDTO;
import org.bugzkit.api.user.mapper.UserMapper;
import org.bugzkit.api.user.payload.dto.UserDTO;
import org.bugzkit.api.user.payload.request.EmailAvailabilityRequest;
import org.bugzkit.api.user.payload.request.UsernameAvailabilityRequest;
import org.bugzkit.api.user.repository.UserRepository;
import org.bugzkit.api.user.service.UserService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;

  public UserServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public PageableDTO<UserDTO> findAll(Pageable pageable) {
    final var users =
        userRepository.findAll(pageable).stream()
            .map(UserMapper.INSTANCE::userToSimpleUserDTO)
            .toList();
    final var total = userRepository.count();
    return new PageableDTO<>(users, total);
  }

  @Override
  @Transactional(readOnly = true)
  public UserDTO findById(Long id) {
    return userRepository
        .findById(id)
        .map(UserMapper.INSTANCE::userToSimpleUserDTO)
        .orElseThrow(
            () -> {
              log.warn("User not found with id '{}'", id);
              return new ResourceNotFoundException("user.notFound");
            });
  }

  @Override
  @Transactional(readOnly = true)
  public UserDTO findByUsername(String username) {
    return userRepository
        .findByUsername(username)
        .map(UserMapper.INSTANCE::userToSimpleUserDTO)
        .orElseThrow(
            () -> {
              log.warn("User not found with username '{}'", username);
              return new ResourceNotFoundException("user.notFound");
            });
  }

  @Override
  @Transactional(readOnly = true)
  public AvailabilityDTO usernameAvailability(
      UsernameAvailabilityRequest usernameAvailabilityRequest) {
    final var available = !userRepository.existsByUsername(usernameAvailabilityRequest.username());
    return new AvailabilityDTO(available);
  }

  @Override
  @Transactional(readOnly = true)
  public AvailabilityDTO emailAvailability(EmailAvailabilityRequest emailAvailabilityRequest) {
    final var available = !userRepository.existsByEmail(emailAvailabilityRequest.email());
    return new AvailabilityDTO(available);
  }
}
