package org.bugzkit.api.user.service.impl;

import java.util.List;
import org.bugzkit.api.user.mapper.UserMapper;
import org.bugzkit.api.user.payload.dto.RoleDTO;
import org.bugzkit.api.user.repository.RoleRepository;
import org.bugzkit.api.user.service.RoleService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@PreAuthorize("hasAuthority('ADMIN')")
public class RoleServiceImpl implements RoleService {
  private final RoleRepository roleRepository;

  public RoleServiceImpl(RoleRepository roleRepository) {
    this.roleRepository = roleRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<RoleDTO> findAll() {
    return roleRepository.findAll().stream().map(UserMapper.INSTANCE::roleToRoleDTO).toList();
  }
}
