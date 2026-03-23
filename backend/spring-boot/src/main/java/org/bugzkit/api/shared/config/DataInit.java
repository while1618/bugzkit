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

@Profile({"dev", "prod"})
@Component
public class DataInit implements ApplicationRunner {
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder bCryptPasswordEncoder;
  private final Environment environment;
  private final Faker faker;
  private Role userRole;
  private Role adminRole;

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
    getRoles();
    seedUsers();
  }

  private void getRoles() {
    userRole = roleRepository.findByName(RoleName.USER).orElseThrow();
    adminRole = roleRepository.findByName(RoleName.ADMIN).orElseThrow();
  }

  private void seedUsers() {
    if (!userRepository.existsByUsername("admin"))
      userRepository.save(
          User.builder()
              .username("admin")
              .email("office@bugzkit.com")
              .password(bCryptPasswordEncoder.encode(password))
              .active(true)
              .lock(false)
              .roles(Set.of(userRole, adminRole))
              .build());
    if (environment.getActiveProfiles()[0].equals("dev")) devUsers();
  }

  private void devUsers() {
    userRepository.save(
        User.builder()
            .username("user")
            .email("user@localhost")
            .password(bCryptPasswordEncoder.encode(password))
            .active(true)
            .lock(false)
            .roles(Collections.singleton(userRole))
            .build());
    List<User> users =
        faker
            .collection(
                () ->
                    User.builder()
                        .username(faker.credentials().username())
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
}
