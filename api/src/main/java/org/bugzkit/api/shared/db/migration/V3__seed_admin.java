package org.bugzkit.api.shared.db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class V3__seed_admin extends BaseJavaMigration {

  private final PasswordEncoder passwordEncoder;

  @Value("${spring.security.user.password}")
  private String password;

  public V3__seed_admin(PasswordEncoder passwordEncoder) {
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void migrate(Context context) throws Exception {
    final var hash = passwordEncoder.encode(password);
    try (var stmt =
        context
            .getConnection()
            .prepareStatement(
                "INSERT INTO users (username, email, password, active, lock, created_at)"
                    + " VALUES ('admin', 'office@bugzkit.com', ?, true, false, NOW())")) {
      stmt.setString(1, hash);
      stmt.executeUpdate();
    }
    try (var stmt =
        context
            .getConnection()
            .prepareStatement(
                "INSERT INTO user_roles (user_id, role_id)"
                    + " SELECT u.user_id, r.role_id FROM users u, roles r"
                    + " WHERE u.username = 'admin' AND r.role_name IN ('USER', 'ADMIN')")) {
      stmt.executeUpdate();
    }
  }
}
