package com.qbaaa.secure.auth.auth.usecase.strategy;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.qbaaa.secure.auth.auth.api.dto.AuthRequest;
import com.qbaaa.secure.auth.auth.api.dto.RefreshTokenRequest;
import com.qbaaa.secure.auth.auth.api.dto.TokenResponse;
import com.qbaaa.secure.auth.auth.domian.service.RefreshTokenService;
import com.qbaaa.secure.auth.auth.domian.service.SessionService;
import com.qbaaa.secure.auth.auth.infrastructure.dto.ClaimJwtDto;
import com.qbaaa.secure.auth.domain.domian.service.DomainService;
import com.qbaaa.secure.auth.domain.infrastructure.projection.DomainConfigValidityProjection;
import com.qbaaa.secure.auth.shared.security.jwt.JwtService;
import com.qbaaa.secure.auth.shared.util.IssuerUtils;
import com.qbaaa.secure.auth.user.domain.service.UserService;
import com.qbaaa.secure.auth.user.infrastructure.entity.RoleEntity;
import com.qbaaa.secure.auth.user.infrastructure.entity.UserEntity;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("refresh-token")
@RequiredArgsConstructor
public class RefreshTokenUseCaseStrategy extends AuthStrategy {

  private static final String CLAIM_SESSION = "session";

  private final JwtService jwtService;
  private final SessionService sessionService;
  private final DomainService domainService;
  private final UserService userService;
  private final RefreshTokenService refreshTokenService;

  @Transactional
  public TokenResponse authenticate(String domainName, String baseUrl, AuthRequest request) {
    var refreshTokenRequest = (RefreshTokenRequest) request;
    var refreshTokenOld = refreshTokenRequest.getToken();
    var issuer = IssuerUtils.buildIssuer(baseUrl, domainName);
    validateRefreshTokenExists(refreshTokenOld);
    var authenticated = verifyToken(issuer, refreshTokenOld);
    var sessionUuid = extractSessionId(authenticated);
    verifySession(sessionUuid);
    final var user = loadUser(sessionUuid);
    final var configDomain = getDomainConfig(domainName);

    final var accessToken =
        generateAccessToken(domainName, baseUrl, sessionUuid, user, configDomain);
    final var refreshTokenNew =
        generateRefreshToken(baseUrl, domainName, sessionUuid, configDomain);

    rotateToken(user, configDomain, refreshTokenOld, refreshTokenNew);

    return new TokenResponse(accessToken, refreshTokenNew);
  }

  private void validateRefreshTokenExists(String token) {
    if (!refreshTokenService.existsRefreshToken(token)) {
      throw new BadCredentialsException("Refresh token not found");
    }
  }

  private DecodedJWT verifyToken(String issuer, String token) {
    return jwtService
        .verify(issuer, token)
        .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));
  }

  private UUID extractSessionId(DecodedJWT jwt) {
    try {
      return UUID.fromString(jwt.getClaim(CLAIM_SESSION).asString());
    } catch (Exception e) {
      throw new BadCredentialsException("Invalid session claim");
    }
  }

  private void verifySession(UUID sessionId) {
    if (!sessionService.verify(sessionId)) {
      throw new BadCredentialsException("User session expired!");
    }
  }

  private UserEntity loadUser(UUID sessionId) {
    return userService
        .findUserBySession(sessionId)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
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

  private void rotateToken(
      UserEntity user, DomainConfigValidityProjection config, String oldToken, String newToken) {
    refreshTokenService.deleteRefreshToken(oldToken);
    refreshTokenService.createRefreshToken(user, config.getRefreshTokenValidity(), newToken);
  }
}
