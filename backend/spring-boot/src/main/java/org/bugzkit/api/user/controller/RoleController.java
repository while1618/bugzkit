package org.bugzkit.api.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.bugzkit.api.shared.constants.Path;
import org.bugzkit.api.shared.error.ErrorMessage;
import org.bugzkit.api.user.payload.dto.RoleDTO;
import org.bugzkit.api.user.service.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "roles")
@SecurityRequirement(name = "cookieAuth")
@RestController
@RequestMapping(Path.ROLES)
public class RoleController {
  private final RoleService roleService;

  public RoleController(RoleService roleService) {
    this.roleService = roleService;
  }

  @Operation(summary = "List all available roles")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized",
        content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
    @ApiResponse(
        responseCode = "403",
        description = "Forbidden",
        content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  })
  @GetMapping
  public ResponseEntity<List<RoleDTO>> findAll() {
    return ResponseEntity.ok(roleService.findAll());
  }
}
