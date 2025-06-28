package com.qbaaa.secure.auth.service.strategy;

import com.qbaaa.secure.auth.config.security.jwt.JwtService;
import com.qbaaa.secure.auth.dto.AuthRequest;
import com.qbaaa.secure.auth.dto.ClaimJwtDto;
import com.qbaaa.secure.auth.dto.LoginRequest;
import com.qbaaa.secure.auth.dto.TokenResponse;
import com.qbaaa.secure.auth.entity.RoleEntity;
import com.qbaaa.secure.auth.entity.UserEntity;
import com.qbaaa.secure.auth.exception.LoginException;
import com.qbaaa.secure.auth.exception.UserNoActiveAccount;
import com.qbaaa.secure.auth.projection.DomainConfigValidityProjection;
import com.qbaaa.secure.auth.service.DomainService;
import com.qbaaa.secure.auth.service.PasswordService;
import com.qbaaa.secure.auth.service.RefreshTokenService;
import com.qbaaa.secure.auth.service.SessionServer;
import com.qbaaa.secure.auth.service.UserService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("password")
@RequiredArgsConstructor
public class LoginStrategyService extends AuthStrategy {

  private final DomainService domainService;
  private final UserService userService;
  private final PasswordService passwordService;
  private final SessionServer sessionServer;
  private final RefreshTokenService refreshTokenService;
  private final JwtService jwtService;

  @Transactional
  public TokenResponse authenticate(String domainName, String baseUrl, AuthRequest request) {
    var login = (LoginRequest) request;
    var user = authenticateUser(domainName, login);
    validateUserIsActive(domainName, user);

    final var configDomain = getDomainConfig(domainName);
    final var session = sessionServer.createSession(user, configDomain.getSessionValidity());

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
}
