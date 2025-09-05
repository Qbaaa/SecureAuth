package com.qbaaa.secure.auth.shared.security;

import com.qbaaa.secure.auth.user.domain.service.PasswordService;
import com.qbaaa.secure.auth.user.domain.service.UserService;
import com.qbaaa.secure.auth.user.infrastructure.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

@RequiredArgsConstructor
@Slf4j
public class UsernamePasswordAuthProvider implements AuthenticationProvider {

  private final UserService userService;
  private final PasswordService passwordService;
  private final CompromisedPasswordChecker compromisedPasswordChecker;

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    if (authentication instanceof CustomUsernamePasswordAuthenticationToken token) {
      String username = (String) token.getPrincipal();
      String password = (String) token.getCredentials();
      String domainName = (String) token.getEnvironment();

      UserEntity user = authenticateUser(domainName, username, password);
      validatePassword(password);

      return CustomUsernamePasswordAuthenticationToken.authenticated(domainName, user, null, null);
    }

    return null;
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return CustomUsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
  }

  private UserEntity authenticateUser(String domainName, String username, String password) {
    var userOpt = userService.findUserInDomain(domainName, username);
    if (userOpt.isEmpty()) {
      throw new BadCredentialsException("Bad username or password");
    }

    var user = userOpt.get();
    if (!passwordService.validatePassword(user, password)) {
      throw new BadCredentialsException("Bad username or password");
    }
    return user;
  }

  private void validatePassword(final String password) {
    if (!password.isBlank() && compromisedPasswordChecker.check(password).isCompromised()) {
      log.warn(
          "The password unsafe because it's well-known to hackers. You must change your password.");
    }
  }
}
