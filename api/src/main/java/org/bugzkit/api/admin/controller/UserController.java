package org.bugzkit.api.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.bugzkit.api.admin.payload.request.PatchUserRequest;
import org.bugzkit.api.admin.payload.request.UserRequest;
import org.bugzkit.api.admin.service.UserService;
import org.bugzkit.api.shared.constants.Path;
import org.bugzkit.api.shared.error.ErrorMessage;
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

  @Operation(summary = "Partially update a user by ID")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(
        responseCode = "400",
        description = "Validation error",
        content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized",
        content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
    @ApiResponse(
        responseCode = "403",
        description = "Forbidden",
        content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Not found",
        content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
    @ApiResponse(
        responseCode = "409",
        description = "Conflict — username or email already exists",
        content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  })
  @PatchMapping("/{id}")
  public ResponseEntity<UserDTO> patch(
      @PathVariable Long id, @Valid @RequestBody PatchUserRequest patchUserRequest) {
    return ResponseEntity.ok(userService.patch(id, patchUserRequest));
  }
}
