package org.bugzkit.api.shared.config;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import net.datafaker.Faker;
import org.bugzkit.api.user.model.Role;
import org.bugzkit.api.user.model.Role.RoleName;
import org.bugzkit.api.user.model.User;
import org.bugzkit.api.user.repository.RoleRepository;
import org.bugzkit.api.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Profile({"dev", "prod", "test"})
@Component
public class DataInit implements ApplicationRunner {
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder bCryptPasswordEncoder;
  private final Environment environment;
  private final Faker faker;
  private final Role userRole = new Role(RoleName.USER);
  private final Role adminRole = new Role(RoleName.ADMIN);

  @Value("${spring.security.user.password}")
  private String password;

  public DataInit(
      UserRepository userRepository,
      RoleRepository roleRepository,
      PasswordEncoder bCryptPasswordEncoder,
      Environment environment) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    this.environment = environment;
    this.faker = new Faker();
  }

  @Override
  public void run(ApplicationArguments args) {
    saveRoles();
    saveUsers();
  }

  private void saveRoles() {
    roleRepository.saveAll(List.of(userRole, adminRole));
  }

  private void saveUsers() {
    userRepository.saveAll(
        List.of(
            User.builder()
                .username("admin")
                .email("admin@localhost")
                .password(bCryptPasswordEncoder.encode(password))
                .active(true)
                .lock(false)
                .roles(Set.of(userRole, adminRole))
                .build(),
            User.builder()
                .username("user")
                .email("user@localhost")
                .password(bCryptPasswordEncoder.encode(password))
                .active(true)
                .lock(false)
                .roles(Collections.singleton(userRole))
                .build()));
    if (environment.getActiveProfiles()[0].equals("dev")) devUsers();
    else if (environment.getActiveProfiles()[0].equals("test")) testUsers();
  }

  private void devUsers() {
    List<User> users =
        faker
            .collection(
                () ->
                    User.builder()
                        .username(faker.internet().username())
                        .email(faker.internet().emailAddress())
                        .password(bCryptPasswordEncoder.encode(password))
                        .active(true)
                        .lock(false)
                        .roles(Collections.singleton(userRole))
                        .build())
            .len(100)
            .generate();
    userRepository.saveAll(users);
  }

  private void testUsers() {
    userRepository.saveAll(
        List.of(
            User.builder()
                .username("deactivated1")
                .email("deactivate1d@localhost")
                .password(bCryptPasswordEncoder.encode(password))
                .active(false)
                .lock(false)
                .roles(Collections.singleton(userRole))
                .build(),
            User.builder()
                .username("deactivated2")
                .email("deactivated2@localhost")
                .password(bCryptPasswordEncoder.encode(password))
                .active(false)
                .lock(false)
                .roles(Collections.singleton(userRole))
                .build(),
            User.builder()
                .username("deactivated3")
                .email("deactivated3@localhost")
                .password(bCryptPasswordEncoder.encode(password))
                .active(false)
                .lock(false)
                .roles(Collections.singleton(userRole))
                .build(),
            User.builder()
                .username("locked")
                .email("locked@localhost")
                .password(bCryptPasswordEncoder.encode(password))
                .active(true)
                .lock(true)
                .roles(Collections.singleton(userRole))
                .build(),
            User.builder()
                .username("update1")
                .email("update1@localhost")
                .password(bCryptPasswordEncoder.encode(password))
                .active(true)
                .lock(false)
                .roles(Collections.singleton(userRole))
                .build(),
            User.builder()
                .username("update2")
                .email("update2@localhost")
                .password(bCryptPasswordEncoder.encode(password))
                .active(true)
                .lock(false)
                .roles(Collections.singleton(userRole))
                .build(),
            User.builder()
                .username("update3")
                .email("update3@localhost")
                .password(bCryptPasswordEncoder.encode(password))
                .active(true)
                .lock(false)
                .roles(Collections.singleton(userRole))
                .build(),
            User.builder()
                .username("update4")
                .email("update4@localhost")
                .password(bCryptPasswordEncoder.encode(password))
                .active(true)
                .lock(false)
                .roles(Collections.singleton(userRole))
                .build(),
            User.builder()
                .username("delete1")
                .email("delete1@localhost")
                .password(bCryptPasswordEncoder.encode(password))
                .active(true)
                .lock(false)
                .roles(Collections.singleton(userRole))
                .build(),
            User.builder()
                .username("delete2")
                .email("delete2@localhost")
                .password(bCryptPasswordEncoder.encode(password))
                .active(true)
                .lock(false)
                .roles(Collections.singleton(userRole))
                .build()));
  }
}
