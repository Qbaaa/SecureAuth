package com.qbaaa.secure.auth.service;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.qbaaa.secure.auth.config.security.jwt.JwtService;
import com.qbaaa.secure.auth.dto.RefreshTokenRequest;
import com.qbaaa.secure.auth.dto.RegisterRequest;
import com.qbaaa.secure.auth.entity.DomainEntity;
import com.qbaaa.secure.auth.entity.UserEntity;
import com.qbaaa.secure.auth.event.AccountActiveEvent;
import com.qbaaa.secure.auth.exception.RegisterException;
import com.qbaaa.secure.auth.util.IssuerUtils;
import jakarta.persistence.EntityNotFoundException;
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
  private static final String CLAIM_USERNAME = "username";

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
    validateRegisterEnabled(domain);
    var user = userService.register(domain, registerRequest);

    return Boolean.TRUE.equals(domain.getIsEnabledVerifiedEmail())
        ? sendVerificationEmail(baseUrl, domainName, domain, user)
        : "Created active account";
  }

  @Transactional
  public void activeAccount(String baseUrl, String domainName, String token) {
    var issuer = IssuerUtils.buildIssuer(baseUrl, domainName);
    validateEmailTokenExists(token);
    var jwt = verifyToken(issuer, token, "Invalid email token");
    var username = jwt.getClaim(CLAIM_USERNAME).asString();
    userService.activeAccount(domainName, username);
    emailTokenService.deleteEmailToken(token);
  }

  @Transactional
  public void logout(String domainName, String baseUrl, RefreshTokenRequest refreshTokenRequest) {
    var refreshToken = refreshTokenRequest.getToken();
    validateRefreshTokenExists(refreshToken);
    var issuer = IssuerUtils.buildIssuer(baseUrl, domainName);
    var jwt = verifyToken(issuer, refreshToken, "Invalid refresh token");
    var sessionUuid = extractSessionId(jwt);

    refreshTokenService.deleteRefreshToken(refreshToken);
    sessionServer.delete(sessionUuid);
  }

  private void validateRegisterEnabled(DomainEntity domain) {
    if (Boolean.FALSE.equals(domain.getIsEnabledRegister())) {
      throw new RegisterException("Registration is disabled for this domain");
    }
  }

  private String sendVerificationEmail(
      String baseUrl, String domainName, DomainEntity domain, UserEntity user) {
    var token =
        jwtService.createActiveAccountToken(
            baseUrl, domainName, domain.getEmailTokenValidity(), user.getUsername());

    emailTokenService.createEmailToken(user, domain.getEmailTokenValidity(), token);

    eventPublisher.publishEvent(
        new AccountActiveEvent(baseUrl, domainName, user.getUsername(), user.getEmail(), token));

    return "Sent email with link activate account";
  }

  private void validateEmailTokenExists(String token) {
    if (!emailTokenService.existsEmailToken(token)) {
      throw new EntityNotFoundException("Email token not found");
    }
  }

  private void validateRefreshTokenExists(String token) {
    if (!refreshTokenService.existsRefreshToken(token)) {
      throw new BadCredentialsException("Refresh token not found");
    }
  }

  private DecodedJWT verifyToken(String issuer, String token, String errorMessage) {
    return jwtService
        .verify(issuer, token)
        .orElseThrow(() -> new BadCredentialsException(errorMessage));
  }

  private UUID extractSessionId(DecodedJWT jwt) {
    return UUID.fromString(jwt.getClaim(CLAIM_SESSION).asString());
  }
}
