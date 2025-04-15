package com.qbaaa.secure.auth.service;

import com.qbaaa.secure.auth.config.security.jwt.JwtService;
import com.qbaaa.secure.auth.dto.RefreshTokenRequest;
import com.qbaaa.secure.auth.dto.RegisterRequest;
import com.qbaaa.secure.auth.event.AccountActiveEvent;
import com.qbaaa.secure.auth.exception.RegisterException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

  private static final String CLAIM_SESSION = "session";

  private final JwtService jwtService;
  private final SessionServer sessionServer;
  private final RefreshTokenService refreshTokenService;
  private final DomainService domainService;
  private final UserService userService;
  private final ApplicationEventPublisher eventPublisher;
  private final EmailTokenService emailTokenService;

  @Transactional
  public String register(String baseUrl, String domainName, RegisterRequest registerRequest) {

    var domain = domainService.getDomain(domainName);
    if (Boolean.FALSE.equals(domain.getIsEnabledRegister())) {
      throw new RegisterException("Registration is disabled for this domain");
    }

    var user = userService.register(domain, registerRequest);

    if (Boolean.TRUE.equals(domain.getIsEnabledVerifiedEmail())) {
      var token =
          jwtService.createActiveAccountToken(baseUrl, domainName, domain.getEmailTokenValidity());
      emailTokenService.createEmailToken(user, domain.getEmailTokenValidity(), token);
      var event =
          new AccountActiveEvent(
              baseUrl, domain.getName(), user.getUsername(), user.getEmail(), token);
      eventPublisher.publishEvent(event);
      return "Sent email with link activate account";
    }

    return "Created active account";
  }

  @Transactional
  public void logout(String domainName, String baseUrl, RefreshTokenRequest refreshTokenRequest) {
    var refreshToken = refreshTokenRequest.getToken();
    if (!refreshTokenService.existsRefreshToken(refreshToken)) {
      throw new BadCredentialsException("Refresh token not found");
    }
    var issuer = baseUrl + "/domains/" + domainName;
    var authenticated =
        jwtService
            .verify(issuer, refreshToken)
            .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

    var session = authenticated.getClaim(CLAIM_SESSION).asString();
    var sessionUuid = UUID.fromString(session);

    refreshTokenService.deleteRefreshToken(refreshToken);
    sessionServer.delete(sessionUuid);
  }
}
