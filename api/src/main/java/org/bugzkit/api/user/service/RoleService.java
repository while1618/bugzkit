package org.bugzkit.api.user.service;

import java.util.List;
import org.bugzkit.api.user.payload.dto.RoleDTO;

public interface RoleService {
  List<RoleDTO> findAll();
}
