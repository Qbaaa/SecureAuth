package com.qbaaa.secure.auth.auth.infrastructure.event;

import com.qbaaa.secure.auth.shared.config.security.CustomUsernamePasswordAuthenticationToken;
import com.qbaaa.secure.auth.user.domain.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationFailureListener {

  private final UserService userService;

  @EventListener
  public void onApplicationEvent(final AbstractAuthenticationFailureEvent event) {
    Authentication auth = event.getAuthentication();

    if (auth instanceof CustomUsernamePasswordAuthenticationToken usernamePasswordToken) {
      var username = (String) usernamePasswordToken.getPrincipal();
      var domainName = (String) usernamePasswordToken.getEnvironment();

      userService.recordFailedLoginAttemptIfExists(domainName, username);
    }
  }
}
