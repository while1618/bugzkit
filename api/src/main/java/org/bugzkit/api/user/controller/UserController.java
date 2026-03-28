package org.bugzkit.api.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.bugzkit.api.shared.constants.Path;
import org.bugzkit.api.shared.error.ErrorMessage;
import org.bugzkit.api.shared.interceptor.RateLimit;
import org.bugzkit.api.shared.payload.dto.AvailabilityDTO;
import org.bugzkit.api.shared.payload.dto.PageableDTO;
import org.bugzkit.api.user.payload.dto.UserDTO;
import org.bugzkit.api.user.payload.request.EmailAvailabilityRequest;
import org.bugzkit.api.user.payload.request.UsernameAvailabilityRequest;
import org.bugzkit.api.user.service.UserService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "users")
@RestController
@RequestMapping(Path.USERS)
public class UserController {
  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @Operation(summary = "List all users (paginated)")
  @ApiResponse(responseCode = "200", description = "OK")
  @GetMapping
  public ResponseEntity<PageableDTO<UserDTO>> findAll(Pageable pageable) {
    return ResponseEntity.ok(userService.findAll(pageable));
  }

  @Operation(summary = "Get a user by ID")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(
        responseCode = "404",
        description = "User not found",
        content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  })
  @GetMapping("/{id}")
  public ResponseEntity<UserDTO> findById(@PathVariable Long id) {
    return ResponseEntity.ok(userService.findById(id));
  }

  @Operation(summary = "Get a user by username")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(
        responseCode = "404",
        description = "User not found",
        content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  })
  @GetMapping("/username/{username}")
  public ResponseEntity<UserDTO> findByUsername(@PathVariable String username) {
    return ResponseEntity.ok(userService.findByUsername(username));
  }

  @Operation(summary = "Check if a username is available")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(
        responseCode = "400",
        description = "Validation error",
        content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
    @ApiResponse(responseCode = "429", description = "Too many requests", content = @Content)
  })
  @RateLimit(requests = 10, duration = 60)
  @PostMapping("/username/availability")
  public ResponseEntity<AvailabilityDTO> usernameAvailability(
      @Valid @RequestBody UsernameAvailabilityRequest usernameAvailabilityRequest) {
    return ResponseEntity.ok(userService.usernameAvailability(usernameAvailabilityRequest));
  }

  @Operation(summary = "Check if an email is available")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(
        responseCode = "400",
        description = "Validation error",
        content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
    @ApiResponse(responseCode = "429", description = "Too many requests", content = @Content)
  })
  @RateLimit(requests = 10, duration = 60)
  @PostMapping("/email/availability")
  public ResponseEntity<AvailabilityDTO> emailAvailability(
      @Valid @RequestBody EmailAvailabilityRequest emailAvailabilityRequest) {
    return ResponseEntity.ok(userService.emailAvailability(emailAvailabilityRequest));
  }
}
