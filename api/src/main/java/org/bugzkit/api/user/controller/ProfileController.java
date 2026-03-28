package org.bugzkit.api.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.bugzkit.api.shared.constants.Path;
import org.bugzkit.api.shared.error.ErrorMessage;
import org.bugzkit.api.shared.interceptor.RateLimit;
import org.bugzkit.api.user.payload.dto.UserDTO;
import org.bugzkit.api.user.payload.request.ChangePasswordRequest;
import org.bugzkit.api.user.payload.request.PatchProfileRequest;
import org.bugzkit.api.user.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "profile")
@SecurityRequirement(name = "cookieAuth")
@RestController
@RequestMapping(Path.PROFILE)
public class ProfileController {
  private final ProfileService profileService;

  public ProfileController(ProfileService profileService) {
    this.profileService = profileService;
  }

  @Operation(summary = "Get the current user's profile")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized",
        content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  })
  @GetMapping
  public ResponseEntity<UserDTO> find() {
    return ResponseEntity.ok(profileService.find());
  }

  @Operation(summary = "Update the current user's profile fields")
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
        responseCode = "409",
        description = "Username or email already exists",
        content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  })
  @PatchMapping
  public ResponseEntity<UserDTO> patch(
      @Valid @RequestBody PatchProfileRequest patchProfileRequest) {
    return ResponseEntity.ok(profileService.patch(patchProfileRequest));
  }

  @Operation(summary = "Delete the current user's account")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Account deleted"),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized",
        content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  })
  @DeleteMapping
  public ResponseEntity<Void> delete() {
    profileService.delete();
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Change the current user's password")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Password changed"),
    @ApiResponse(
        responseCode = "400",
        description = "Current password is wrong or validation error",
        content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized",
        content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
    @ApiResponse(responseCode = "429", description = "Too many requests", content = @Content)
  })
  @RateLimit(requests = 5, duration = 60)
  @PatchMapping("/password")
  public ResponseEntity<Void> changePassword(
      @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
    profileService.changePassword(changePasswordRequest);
    return ResponseEntity.noContent().build();
  }
}
