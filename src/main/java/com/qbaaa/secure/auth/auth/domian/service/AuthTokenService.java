package com.qbaaa.secure.auth.auth.domian.service;

import com.qbaaa.secure.auth.auth.api.dto.TokenResponse;
import com.qbaaa.secure.auth.auth.infrastructure.dto.ClaimJwtDto;
import com.qbaaa.secure.auth.domain.domian.service.DomainService;
import com.qbaaa.secure.auth.domain.infrastructure.projection.DomainConfigValidityProjection;
import com.qbaaa.secure.auth.shared.security.jwt.JwtService;
import com.qbaaa.secure.auth.user.infrastructure.entity.RoleEntity;
import com.qbaaa.secure.auth.user.infrastructure.entity.UserEntity;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthTokenService {

  private final DomainService domainService;
  private final SessionService sessionService;
  private final RefreshTokenService refreshTokenService;
  private final JwtService jwtService;

  public TokenResponse createToken(String baseUrl, String domainName, UserEntity user) {
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
