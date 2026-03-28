package org.bugzkit.api.user.service;

import org.bugzkit.api.user.payload.dto.UserDTO;
import org.bugzkit.api.user.payload.request.ChangePasswordRequest;
import org.bugzkit.api.user.payload.request.PatchProfileRequest;

public interface ProfileService {
  UserDTO find();

  UserDTO patch(PatchProfileRequest patchProfileRequest);

  void delete();

  void changePassword(ChangePasswordRequest changePasswordRequest);
}
