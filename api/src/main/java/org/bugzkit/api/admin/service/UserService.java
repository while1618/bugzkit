package org.bugzkit.api.admin.service;

import org.bugzkit.api.admin.payload.request.PatchUserRequest;
import org.bugzkit.api.admin.payload.request.UserRequest;
import org.bugzkit.api.shared.generic.crud.CrudService;
import org.bugzkit.api.user.payload.dto.UserDTO;

public interface UserService extends CrudService<UserDTO, UserRequest> {
  UserDTO patch(Long id, PatchUserRequest patchUserRequest);
}
