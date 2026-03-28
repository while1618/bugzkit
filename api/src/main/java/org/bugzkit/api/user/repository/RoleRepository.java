package org.bugzkit.api.user.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.bugzkit.api.user.model.Role;
import org.bugzkit.api.user.model.Role.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
  List<Role> findAllByNameIn(Set<RoleName> roleNames);

  Optional<Role> findByName(RoleName roleName);
}
