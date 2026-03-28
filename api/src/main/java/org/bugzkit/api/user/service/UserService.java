package org.bugzkit.api.user.service;

import org.bugzkit.api.shared.payload.dto.AvailabilityDTO;
import org.bugzkit.api.shared.payload.dto.PageableDTO;
import org.bugzkit.api.user.payload.dto.UserDTO;
import org.bugzkit.api.user.payload.request.EmailAvailabilityRequest;
import org.bugzkit.api.user.payload.request.UsernameAvailabilityRequest;
import org.springframework.data.domain.Pageable;

public interface UserService {
  PageableDTO<UserDTO> findAll(Pageable pageable);

  UserDTO findById(Long id);

  UserDTO findByUsername(String username);

  AvailabilityDTO usernameAvailability(UsernameAvailabilityRequest usernameAvailabilityRequest);

  AvailabilityDTO emailAvailability(EmailAvailabilityRequest emailAvailabilityRequest);
}
