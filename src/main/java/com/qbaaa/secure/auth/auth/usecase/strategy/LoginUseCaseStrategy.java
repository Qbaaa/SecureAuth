package com.qbaaa.secure.auth.auth.usecase.strategy;

import com.qbaaa.secure.auth.auth.api.dto.AuthRequest;
import com.qbaaa.secure.auth.auth.api.dto.LoginRequest;
import com.qbaaa.secure.auth.auth.api.dto.TokenResponse;
import com.qbaaa.secure.auth.auth.domian.service.RefreshTokenService;
import com.qbaaa.secure.auth.auth.domian.service.SessionService;
import com.qbaaa.secure.auth.auth.infrastructure.dto.ClaimJwtDto;
import com.qbaaa.secure.auth.domain.domian.service.DomainService;
import com.qbaaa.secure.auth.domain.infrastructure.projection.DomainConfigValidityProjection;
import com.qbaaa.secure.auth.shared.config.security.CustomUsernamePasswordAuthenticationToken;
import com.qbaaa.secure.auth.shared.config.security.jwt.JwtService;
import com.qbaaa.secure.auth.shared.exception.UserNoActiveAccount;
import com.qbaaa.secure.auth.user.infrastructure.entity.RoleEntity;
import com.qbaaa.secure.auth.user.infrastructure.entity.UserEntity;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("password")
@RequiredArgsConstructor
public class LoginUseCaseStrategy extends AuthStrategy {

  private final AuthenticationManager authenticationManager;
  private final DomainService domainService;
  private final SessionService sessionService;
  private final RefreshTokenService refreshTokenService;
  private final JwtService jwtService;

  @Transactional
  public TokenResponse authenticate(String domainName, String baseUrl, AuthRequest request) {
    var login = (LoginRequest) request;

    Authentication authentication =
        authenticationManager.authenticate(
            CustomUsernamePasswordAuthenticationToken.unauthenticated(
                domainName, login.getUsername(), login.getPassword()));

    var user = (UserEntity) authentication.getPrincipal();
    validateUserIsActive(domainName, user);

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
