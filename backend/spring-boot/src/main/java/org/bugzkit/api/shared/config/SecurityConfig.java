package org.bugzkit.api.shared.config;

import org.bugzkit.api.auth.oauth2.OAuth2FailureHandler;
import org.bugzkit.api.auth.oauth2.OAuth2SuccessHandler;
import org.bugzkit.api.auth.oauth2.OAuth2UserService;
import org.bugzkit.api.auth.security.JWTFilter;
import org.bugzkit.api.auth.security.UserDetailsServiceImpl;
import org.bugzkit.api.shared.constants.Path;
import org.bugzkit.api.shared.error.handling.CustomAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
  private static final String[] OAUTH2_WHITELIST = {"/oauth2/authorization/google"};
  private static final String[] AUTH_WHITELIST = {
    Path.AUTH + "/register",
    Path.AUTH + "/tokens",
    Path.AUTH + "/tokens/refresh",
    Path.AUTH + "/password/forgot",
    Path.AUTH + "/password/reset",
    Path.AUTH + "/verification-email",
    Path.AUTH + "/verify-email",
  };
  private static final String[] USERS_WHITELIST = {Path.USERS, Path.USERS + "/**"};
  private static final String[] SWAGGER_WHITELIST = {
    "/swagger-ui/**", "/v3/api-docs/**", "/openapi.yml"
  };
  private final JWTFilter jwtFilter;
  private final UserDetailsServiceImpl userDetailsService;
  private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
  private final OAuth2SuccessHandler oAuth2SuccessHandler;
  private final OAuth2FailureHandler oAuth2FailureHandler;
  private final OAuth2UserService oAuth2UserService;

  public SecurityConfig(
      JWTFilter jwtFilter,
      UserDetailsServiceImpl userDetailsService,
      CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
      OAuth2SuccessHandler oAuth2SuccessHandler,
      OAuth2FailureHandler oAuth2FailureHandler,
      OAuth2UserService oAuth2UserService) {
    this.jwtFilter = jwtFilter;
    this.userDetailsService = userDetailsService;
    this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
    this.oAuth2SuccessHandler = oAuth2SuccessHandler;
    this.oAuth2FailureHandler = oAuth2FailureHandler;
    this.oAuth2UserService = oAuth2UserService;
  }

  @Bean
  public PasswordEncoder bCryptPasswordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

  @Bean
  public AuthenticationManager authenticationManager() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
    authProvider.setPasswordEncoder(bCryptPasswordEncoder());
    return new ProviderManager(authProvider);
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(AUTH_WHITELIST)
                    .permitAll()
                    .requestMatchers(HttpMethod.DELETE, Path.AUTH + "/tokens/devices")
                    .permitAll()
                    .requestMatchers(USERS_WHITELIST)
                    .permitAll()
                    .requestMatchers(SWAGGER_WHITELIST)
                    .permitAll()
                    .requestMatchers(OAUTH2_WHITELIST)
                    .permitAll()
                    .requestMatchers("/favicon.ico")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2Login(
            oauth ->
                oauth
                    .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserService))
                    .successHandler(oAuth2SuccessHandler)
                    .failureHandler(oAuth2FailureHandler))
        .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterAfter(jwtFilter, UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling(
            exception -> exception.authenticationEntryPoint(customAuthenticationEntryPoint))
        .cors(cors -> cors.configure(http))
        .build();
  }
}
