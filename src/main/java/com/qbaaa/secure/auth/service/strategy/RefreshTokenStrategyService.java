package com.qbaaa.secure.auth.service.strategy;

import com.qbaaa.secure.auth.config.security.jwt.JwtService;
import com.qbaaa.secure.auth.dto.AuthRequest;
import com.qbaaa.secure.auth.dto.ClaimJwtDto;
import com.qbaaa.secure.auth.dto.RefreshTokenRequest;
import com.qbaaa.secure.auth.dto.TokenResponse;
import com.qbaaa.secure.auth.entity.RoleEntity;
import com.qbaaa.secure.auth.service.DomainService;
import com.qbaaa.secure.auth.service.RefreshTokenService;
import com.qbaaa.secure.auth.service.SessionServer;
import com.qbaaa.secure.auth.service.UserService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("refresh-token")
@RequiredArgsConstructor
public class RefreshTokenStrategyService extends AuthStrategy {

  private static final String CLAIM_SESSION = "session";

  private final JwtService jwtService;
  private final SessionServer sessionServer;
  private final DomainService domainService;
  private final UserService userService;
  private final RefreshTokenService refreshTokenService;

  @Transactional
  public TokenResponse authenticate(String domainName, String baseUrl, AuthRequest request) {
    var refreshTokenRequest = (RefreshTokenRequest) request;
    var refreshTokenOld = refreshTokenRequest.getToken();
    var issuer = baseUrl + "/domains/" + domainName;
    if (!refreshTokenService.existsRefreshToken(refreshTokenOld)) {
      throw new BadCredentialsException("Refresh token not found");
    }

    var authenticated =
        jwtService
            .verify(issuer, refreshTokenOld)
            .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

    var session = authenticated.getClaim(CLAIM_SESSION).asString();
    var sessionUuid = UUID.fromString(session);
    if (!sessionServer.verify(sessionUuid)) {
      throw new BadCredentialsException("User session expired!");
    }

    final var user =
        userService
            .findUserBySession(sessionUuid)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    final var configDomain = domainService.getDomainConfigValidity(domainName);
    final var roles = user.getRoles().stream().map(RoleEntity::getName).toList();
    final var claimJwtDto =
        new ClaimJwtDto(
            domainName,
            user.getUsername(),
            user.getEmail(),
            roles,
            baseUrl,
            refreshTokenOld,
            configDomain.getAccessTokenValidity());
    final var accessToken = jwtService.createAccessToken(claimJwtDto);
    final var refreshTokenNew =
        jwtService.createRefreshToken(
            baseUrl, domainName, session, configDomain.getRefreshTokenValidity());

    refreshTokenService.deleteRefreshToken(refreshTokenOld);
    refreshTokenService.createRefreshToken(
        user, configDomain.getRefreshTokenValidity(), refreshTokenNew);

    return new TokenResponse(accessToken, refreshTokenNew);
  }
}
