package com.qbaaa.secure.auth.auth.usecase.strategy;

import com.qbaaa.secure.auth.auth.api.dto.AuthRequest;
import com.qbaaa.secure.auth.auth.api.dto.LoginRequest;
import com.qbaaa.secure.auth.auth.api.dto.TokenResponse;
import com.qbaaa.secure.auth.auth.domian.service.RefreshTokenService;
import com.qbaaa.secure.auth.auth.domian.service.SessionService;
import com.qbaaa.secure.auth.auth.infrastructure.dto.ClaimJwtDto;
import com.qbaaa.secure.auth.domain.domian.service.DomainService;
import com.qbaaa.secure.auth.domain.infrastructure.projection.DomainConfigValidityProjection;
import com.qbaaa.secure.auth.shared.config.security.jwt.JwtService;
import com.qbaaa.secure.auth.shared.exception.LoginException;
import com.qbaaa.secure.auth.shared.exception.UserNoActiveAccount;
import com.qbaaa.secure.auth.user.domain.service.PasswordService;
import com.qbaaa.secure.auth.user.domain.service.UserService;
import com.qbaaa.secure.auth.user.infrastructure.entity.RoleEntity;
import com.qbaaa.secure.auth.user.infrastructure.entity.UserEntity;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("password")
@RequiredArgsConstructor
@Slf4j
public class LoginUseCaseStrategy extends AuthStrategy {

  private final DomainService domainService;
  private final UserService userService;
  private final PasswordService passwordService;
  private final SessionService sessionService;
  private final RefreshTokenService refreshTokenService;
  private final JwtService jwtService;
  private final CompromisedPasswordChecker compromisedPasswordChecker;

  @Transactional
  public TokenResponse authenticate(String domainName, String baseUrl, AuthRequest request) {
    var login = (LoginRequest) request;
    var user = authenticateUser(domainName, login);
    validateUserIsActive(domainName, user);
    validatePassword(login.getPassword());

    final var configDomain = getDomainConfig(domainName);
    final var session = sessionService.createSession(user, configDomain.getSessionValidity());

    final var accessToken =
        generateAccessToken(domainName, baseUrl, session.getSessionToken(), user, configDomain);
    final var refreshToken =
        generateRefreshToken(baseUrl, domainName, session.getSessionToken(), configDomain);

    refreshTokenService.createRefreshToken(
        user, configDomain.getRefreshTokenValidity(), refreshToken);

    return new TokenResponse(accessToken, refreshToken);
  }

  private UserEntity authenticateUser(String domainName, LoginRequest login) {
    var userOpt = userService.findUserInDomain(domainName, login.getUsername());
    if (userOpt.isEmpty()) {
      throw new LoginException("Bad username or password");
    }

    var user = userOpt.get();
    if (!passwordService.validatePassword(user, login.getPassword())) {
      throw new LoginException("Bad username or password");
    }
    return user;
  }

  private void validateUserIsActive(String domainName, UserEntity user) {
    if (BooleanUtils.isFalse(user.getIsActive())) {
      throw new UserNoActiveAccount(user.getUsername(), domainName);
    }
  }

  private DomainConfigValidityProjection getDomainConfig(String domainName) {
    return domainService.getDomainConfigValidity(domainName);
  }

  private String generateAccessToken(
      String domainName,
      String baseUrl,
      UUID sessionId,
      UserEntity user,
      DomainConfigValidityProjection config) {
    var roles = user.getRoles().stream().map(RoleEntity::getName).toList();
    var claims =
        new ClaimJwtDto(
            domainName,
            user.getUsername(),
            user.getEmail(),
            roles,
            baseUrl,
            sessionId.toString(),
            config.getAccessTokenValidity());
    return jwtService.createAccessToken(claims);
  }

  private String generateRefreshToken(
      String baseUrl, String domainName, UUID sessionId, DomainConfigValidityProjection config) {
    return jwtService.createRefreshToken(
        baseUrl, domainName, sessionId.toString(), config.getRefreshTokenValidity());
  }

  private void validatePassword(final String password) {
    if (!password.isBlank() && compromisedPasswordChecker.check(password).isCompromised()) {
      log.warn(
          "The password unsafe because it's well-known to hackers. You must change your password.");
    }
  }
}
