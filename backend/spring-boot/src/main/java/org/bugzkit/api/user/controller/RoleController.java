package org.bugzkit.api.user.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.bugzkit.api.shared.constants.Path;
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

  @GetMapping
  public ResponseEntity<List<RoleDTO>> findAll() {
    return ResponseEntity.ok(roleService.findAll());
  }
}
