package com.qbaaa.secure.auth.user.usecase;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.qbaaa.secure.auth.shared.config.security.jwt.JwtService;
import com.qbaaa.secure.auth.shared.util.IssuerUtils;
import com.qbaaa.secure.auth.user.domain.service.EmailTokenService;
import com.qbaaa.secure.auth.user.domain.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActiveAccountUseCase {

  private static final String CLAIM_USERNAME = "username";

  private final JwtService jwtService;
  private final UserService userService;
  private final EmailTokenService emailTokenService;

  @Transactional
  public void activeAccount(String baseUrl, String domainName, String token) {
    var issuer = IssuerUtils.buildIssuer(baseUrl, domainName);
    validateEmailTokenExists(token);
    var jwt = verifyToken(issuer, token, "Invalid email token");
    var username = jwt.getClaim(CLAIM_USERNAME).asString();
    userService.activeAccount(domainName, username);
    emailTokenService.deleteEmailToken(token);
  }

  private void validateEmailTokenExists(String token) {
    if (!emailTokenService.existsEmailToken(token)) {
      throw new EntityNotFoundException("Email token not found");
    }
  }

  private DecodedJWT verifyToken(String issuer, String token, String errorMessage) {
    return jwtService
        .verify(issuer, token)
        .orElseThrow(() -> new BadCredentialsException(errorMessage));
  }
}
