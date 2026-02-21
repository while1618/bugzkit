package org.bugzkit.api.user.controller;

import jakarta.validation.Valid;
import org.bugzkit.api.shared.constants.Path;
import org.bugzkit.api.shared.ratelimit.RateLimit;
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

@RestController
@RequestMapping(Path.PROFILE)
public class ProfileController {
  private final ProfileService profileService;

  public ProfileController(ProfileService profileService) {
    this.profileService = profileService;
  }

  @GetMapping
  public ResponseEntity<UserDTO> find() {
    return ResponseEntity.ok(profileService.find());
  }

  @PatchMapping
  public ResponseEntity<UserDTO> patch(
      @Valid @RequestBody PatchProfileRequest patchProfileRequest) {
    return ResponseEntity.ok(profileService.patch(patchProfileRequest));
  }

  @DeleteMapping
  public ResponseEntity<Void> delete() {
    profileService.delete();
    return ResponseEntity.noContent().build();
  }

  @RateLimit(requests = 5, duration = 60)
  @PatchMapping("/password")
  public ResponseEntity<Void> changePassword(
      @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
    profileService.changePassword(changePasswordRequest);
    return ResponseEntity.noContent().build();
  }
}
