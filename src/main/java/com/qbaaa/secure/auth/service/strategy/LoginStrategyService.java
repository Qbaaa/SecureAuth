package com.qbaaa.secure.auth.service.strategy;

import com.qbaaa.secure.auth.config.security.jwt.JwtService;
import com.qbaaa.secure.auth.dto.AuthRequest;
import com.qbaaa.secure.auth.dto.ClaimJwtDto;
import com.qbaaa.secure.auth.dto.LoginRequest;
import com.qbaaa.secure.auth.dto.TokenResponse;
import com.qbaaa.secure.auth.entity.RoleEntity;
import com.qbaaa.secure.auth.exception.LoginException;
import com.qbaaa.secure.auth.exception.UserNoActiveAccount;
import com.qbaaa.secure.auth.service.DomainService;
import com.qbaaa.secure.auth.service.PasswordService;
import com.qbaaa.secure.auth.service.RefreshTokenService;
import com.qbaaa.secure.auth.service.SessionServer;
import com.qbaaa.secure.auth.service.UserService;
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

    var userOpt = userService.findUserInDomain(domainName, login.getUsername());
    if (userOpt.isEmpty()) {
      throw new LoginException("Bad username or password");
    }

    var user = userOpt.get();
    if (!passwordService.validatePassword(user, login.getPassword())) {
      throw new LoginException("Bad username or password");
    }

    if (BooleanUtils.isFalse(user.getIsActive())) {
      throw new UserNoActiveAccount(user.getUsername(), domainName);
    }

    final var configDomain = domainService.getDomainConfigValidity(domainName);
    final var session = sessionServer.createSession(user, configDomain.getSessionValidity());
    final var roles = user.getRoles().stream().map(RoleEntity::getName).toList();
    final var claimJwtDto =
        new ClaimJwtDto(
            domainName,
            user.getUsername(),
            user.getEmail(),
            roles,
            baseUrl,
            session.getSessionToken().toString(),
            configDomain.getAccessTokenValidity());
    final var accessToken = jwtService.createAccessToken(claimJwtDto);
    final var refreshToken =
        jwtService.createRefreshToken(
            baseUrl,
            domainName,
            session.getSessionToken().toString(),
            configDomain.getRefreshTokenValidity());
    refreshTokenService.createRefreshToken(
        user, configDomain.getRefreshTokenValidity(), refreshToken);

    return new TokenResponse(accessToken, refreshToken);
  }
}
