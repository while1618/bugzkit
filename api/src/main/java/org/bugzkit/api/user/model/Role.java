package org.bugzkit.api.user.model;

import com.google.common.base.Objects;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "roles",
    indexes = @Index(name = "idx_role_name", columnList = "role_name", unique = true))
public class Role implements Serializable {
  @Serial private static final long serialVersionUID = 3717126169522609755L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "role_id")
  private Long id;

  @Column(name = "role_name", unique = true, nullable = false)
  @Enumerated(EnumType.STRING)
  private RoleName name;

  public Role(RoleName name) {
    this.name = name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Role role = (Role) o;
    return Objects.equal(id, role.id) && name == role.name;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id, name);
  }

  public enum RoleName {
    USER,
    ADMIN
  }
}
