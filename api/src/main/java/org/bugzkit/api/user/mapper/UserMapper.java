package org.bugzkit.api.user.mapper;

import java.util.Set;
import org.bugzkit.api.auth.security.UserPrincipal;
import org.bugzkit.api.user.model.Role;
import org.bugzkit.api.user.model.User;
import org.bugzkit.api.user.payload.dto.RoleDTO;
import org.bugzkit.api.user.payload.dto.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserMapper {

  Set<RoleDTO> rolesToRoleDTOs(Set<Role> roles);

  RoleDTO roleToRoleDTO(Role role);

  @Mapping(source = "roles", target = "roleDTOs")
  UserDTO userToAdminUserDTO(User user);

  @Mapping(target = "active", ignore = true)
  @Mapping(target = "lock", ignore = true)
  @Mapping(target = "roleDTOs", ignore = true)
  UserDTO userToProfileUserDTO(User user);

  @Mapping(target = "active", ignore = true)
  @Mapping(target = "lock", ignore = true)
  @Mapping(target = "roleDTOs", ignore = true)
  UserDTO userPrincipalToProfileUserDTO(UserPrincipal userPrincipal);

  @Mapping(target = "email", ignore = true)
  @Mapping(target = "active", ignore = true)
  @Mapping(target = "lock", ignore = true)
  @Mapping(target = "roleDTOs", ignore = true)
  UserDTO userToSimpleUserDTO(User user);
}
