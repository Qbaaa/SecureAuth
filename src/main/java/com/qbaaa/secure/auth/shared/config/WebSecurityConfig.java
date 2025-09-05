package com.qbaaa.secure.auth.shared.config;

import com.qbaaa.secure.auth.shared.security.UsernamePasswordAuthProvider;
import com.qbaaa.secure.auth.shared.security.jwt.CustomJwtAuthenticationFilter;
import com.qbaaa.secure.auth.shared.security.jwt.CustomJwtAuthenticationProvider;
import com.qbaaa.secure.auth.shared.security.jwt.JwtGrantedAuthoritiesConverter;
import com.qbaaa.secure.auth.shared.security.jwt.JwtService;
import com.qbaaa.secure.auth.shared.security.jwt.Role;
import com.qbaaa.secure.auth.user.domain.service.PasswordService;
import com.qbaaa.secure.auth.user.domain.service.UserService;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.password.HaveIBeenPwnedRestApiPasswordChecker;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
public class WebSecurityConfig {

  private static final String[] WHITE_LIST = {
    "/swagger-ui/**",
    "/api-secure-auth-backend/**",
    "/domains/*/auth/token",
    "/domains/*/users/register",
    "/domains/*/users/account/activate",
  };

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public CompromisedPasswordChecker compromisedPasswordChecker() {
    return new HaveIBeenPwnedRestApiPasswordChecker();
  }

  @Bean
  public CorsConfiguration corsConfiguration() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of("*"));
    configuration.setAllowedMethods(List.of("*"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);
    return configuration;
  }

  @Bean
  SecurityFilterChain securityFilterChain(
      HttpSecurity http, CustomJwtAuthenticationFilter customJwtAuthenticationFilter)
      // CustomLoginAuthenticationFilter customLoginFilter)
      throws Exception {
    return http.authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers(WHITE_LIST)
                    .permitAll()
                    .requestMatchers("/admin/domains")
                    .hasRole("master__" + Role.ADMIN.name())
                    .requestMatchers("/domains/*/auth/login/mfa")
                    .hasRole(Role.PENDING_LOGIN_MFA.name())
                    .requestMatchers("/domains/*/auth/logout")
                    .authenticated()
                    .anyRequest()
                    .authenticated())
        .sessionManagement(
            sessionManagement ->
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .csrf(AbstractHttpConfigurer::disable) // default enable
        .cors(config -> config.configurationSource(request -> corsConfiguration()))
        // .cors(AbstractHttpConfigurer::disable)
        .addFilterBefore(customJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        // .addFilterAt(customLoginFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  @Bean
  public AuthenticationProvider customJwtAuthenticationProvider(JwtService jwtService) {
    return new CustomJwtAuthenticationProvider(jwtService, new JwtGrantedAuthoritiesConverter());
  }

  @Bean
  public AuthenticationProvider customUsernamePasswordAuthProvider(
      UserService userService,
      PasswordService passwordService,
      CompromisedPasswordChecker compromisedPasswordChecker) {
    return new UsernamePasswordAuthProvider(
        userService, passwordService, compromisedPasswordChecker);
  }

  @Bean
  public DefaultAuthenticationEventPublisher authenticationEventPublisher(
      ApplicationEventPublisher applicationEventPublisher) {
    return new DefaultAuthenticationEventPublisher(applicationEventPublisher);
  }

  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationProvider customJwtAuthenticationProvider,
      AuthenticationProvider customUsernamePasswordAuthProvider,
      DefaultAuthenticationEventPublisher authenticationEventPublisher) {

    ProviderManager authManager =
        new ProviderManager(
            List.of(customJwtAuthenticationProvider, customUsernamePasswordAuthProvider));

    authManager.setAuthenticationEventPublisher(authenticationEventPublisher);
    return authManager;
  }
}
