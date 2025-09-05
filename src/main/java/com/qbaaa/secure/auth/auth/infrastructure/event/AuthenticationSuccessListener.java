package com.qbaaa.secure.auth.auth.infrastructure.event;

import com.qbaaa.secure.auth.shared.security.CustomUsernamePasswordAuthenticationToken;
import com.qbaaa.secure.auth.user.domain.service.UserService;
import com.qbaaa.secure.auth.user.infrastructure.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationSuccessListener {

  private final UserService userService;

  @EventListener
  public void onApplicationEvent(final AuthenticationSuccessEvent event) {
    Authentication auth = event.getAuthentication();

    if (auth instanceof CustomUsernamePasswordAuthenticationToken usernamePasswordToken) {
      var user = (UserEntity) usernamePasswordToken.getPrincipal();
      //      var domainName = (String) usernamePasswordToken.getEnvironment();
      //      final UserEntity user =
      //              userService.findUserInDomain(domainName, username)
      //                      .orElseThrow(() -> new EntityNotFoundException("User not found in
      // domain"));
      if (Boolean.FALSE.equals(user.getIsMultifactorAuthEnabled())) {
        userService.resetFailedLoginAttempts(user);
      }
    }
  }
}
