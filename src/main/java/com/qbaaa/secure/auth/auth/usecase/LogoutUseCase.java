package com.qbaaa.secure.auth.auth.usecase;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.qbaaa.secure.auth.auth.api.dto.RefreshTokenRequest;
import com.qbaaa.secure.auth.auth.domian.service.RefreshTokenService;
import com.qbaaa.secure.auth.auth.domian.service.SessionService;
import com.qbaaa.secure.auth.shared.security.jwt.JwtService;
import com.qbaaa.secure.auth.shared.util.IssuerUtils;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogoutUseCase {

  private static final String CLAIM_SESSION = "session";

  private final JwtService jwtService;
  private final SessionService sessionService;
  private final RefreshTokenService refreshTokenService;

  @Transactional
  public void logout(String domainName, String baseUrl, RefreshTokenRequest refreshTokenRequest) {
    var refreshToken = refreshTokenRequest.getToken();
    validateRefreshTokenExists(refreshToken);
    var issuer = IssuerUtils.buildIssuer(baseUrl, domainName);
    var jwt = verifyToken(issuer, refreshToken, "Invalid refresh token");
    var sessionUuid = extractSessionId(jwt);

    refreshTokenService.deleteRefreshToken(refreshToken);
    sessionService.delete(sessionUuid);
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
