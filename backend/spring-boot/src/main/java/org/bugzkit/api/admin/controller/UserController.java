package org.bugzkit.api.admin.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.bugzkit.api.admin.payload.request.PatchUserRequest;
import org.bugzkit.api.admin.payload.request.UserRequest;
import org.bugzkit.api.admin.service.UserService;
import org.bugzkit.api.shared.constants.Path;
import org.bugzkit.api.shared.generic.crud.CrudController;
import org.bugzkit.api.user.payload.dto.UserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "admin")
@SecurityRequirement(name = "cookieAuth")
@RestController("adminUserController")
@RequestMapping(Path.ADMIN_USERS)
public class UserController extends CrudController<UserDTO, UserRequest> {
  private final UserService userService;

  public UserController(UserService userService) {
    super(userService);
    this.userService = userService;
  }

  @PatchMapping("/{id}")
  public ResponseEntity<UserDTO> patch(
      @PathVariable("id") Long id, @Valid @RequestBody PatchUserRequest patchUserRequest) {
    return ResponseEntity.ok(userService.patch(id, patchUserRequest));
  }
}
