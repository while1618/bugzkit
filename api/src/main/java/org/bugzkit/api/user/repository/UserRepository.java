package org.bugzkit.api.user.repository;

import java.util.List;
import java.util.Optional;
import org.bugzkit.api.user.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {
  @Query("select u.id from User u order by u.id")
  List<Long> findAllUserIds(Pageable pageable);

  @EntityGraph(attributePaths = "roles")
  List<User> findAllByIdIn(List<Long> ids);

  @EntityGraph(attributePaths = "roles")
  Optional<User> findWithRolesById(Long id);

  Optional<User> findByUsername(String username);

  @EntityGraph(attributePaths = "roles")
  Optional<User> findWithRolesByUsername(String username);

  @EntityGraph(attributePaths = "roles")
  Optional<User> findWithRolesByEmail(String email);

  Optional<User> findByEmail(String email);

  Optional<User> findByUsernameOrEmail(String username, String email);

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);
}
