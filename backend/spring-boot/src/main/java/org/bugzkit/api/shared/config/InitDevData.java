package org.bugzkit.api.shared.config;

import java.util.Collections;
import java.util.List;
import net.datafaker.Faker;
import org.bugzkit.api.user.model.Role.RoleName;
import org.bugzkit.api.user.model.User;
import org.bugzkit.api.user.repository.RoleRepository;
import org.bugzkit.api.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Profile("dev")
@Component
public class InitDevData implements ApplicationRunner {
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final Faker faker;

  @Value("${spring.security.user.password}")
  private String password;

  public InitDevData(
      UserRepository userRepository,
      RoleRepository roleRepository,
      PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.passwordEncoder = passwordEncoder;
    this.faker = new Faker();
  }

  @Override
  public void run(ApplicationArguments args) {
    final var userRole = roleRepository.findByName(RoleName.USER).orElseThrow();
    userRepository.save(
        User.builder()
            .username("user")
            .email("user@localhost")
            .password(passwordEncoder.encode(password))
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
                        .password(passwordEncoder.encode(password))
                        .active(true)
                        .lock(false)
                        .roles(Collections.singleton(userRole))
                        .build())
            .len(100)
            .generate();
    userRepository.saveAll(users);
  }
}
