package com.qbaaa.secure.auth.multifactorauth.usecase;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.qbaaa.secure.auth.auth.api.dto.TokenResponse;
import com.qbaaa.secure.auth.auth.domian.service.AuthTokenService;
import com.qbaaa.secure.auth.multifactorauth.api.dto.MfaRequest;
import com.qbaaa.secure.auth.shared.config.time.TimeProvider;
import com.qbaaa.secure.auth.shared.security.jwt.CustomJwtAuthenticationToken;
import com.qbaaa.secure.auth.shared.security.jwt.JwtUsernameConverter;
import com.qbaaa.secure.auth.user.domain.enums.OperationType;
import com.qbaaa.secure.auth.user.domain.service.OtpService;
import com.qbaaa.secure.auth.user.domain.service.UserService;
import com.qbaaa.secure.auth.user.infrastructure.entity.OtpEntity;
import com.qbaaa.secure.auth.user.infrastructure.entity.UserEntity;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateAuthTokenUseCase {

  private final AuthTokenService authTokenService;
  private final UserService userService;
  private final JwtUsernameConverter jwtUsernameConverter;
  private final TimeProvider timeProvider;
  private final OtpService otpService;

  @Transactional
  public TokenResponse createAuthToken(MfaRequest mfaRequest, String baseURL, String domainName) {
    final String username = getUsername();
    final UserEntity user = getUserOrError(domainName, username);

    if (!verifyOtp(mfaRequest.secret(), domainName, user)) {
      throw new BadCredentialsException("Invalid code!");
    }

    return authTokenService.createToken(baseURL, domainName, user);
  }

  private String getUsername() {
    Object authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication instanceof CustomJwtAuthenticationToken jwtAuthentication) {
      Object principal = jwtAuthentication.getPrincipal();

      if (principal instanceof DecodedJWT jwt) {
        return jwtUsernameConverter.convert(jwt);
      } else {
        throw new IllegalStateException(
            "Unable to process request, unknown authentication principal type: "
                + principal.getClass());
      }
    } else {
      throw new IllegalStateException(
          "Unable to process request, unknown authentication type: " + authentication.getClass());
    }
  }

  public UserEntity getUserOrError(final String domainName, final String username) {
    return userService
        .findUserInDomain(domainName, username)
        .orElseThrow(() -> new BadCredentialsException("Invalid token"));
  }

  private boolean verifyOtp(String inputOtp, String domainName, UserEntity user) {
    final Optional<OtpEntity> otpUser = user.getOtp();
    final boolean validToken =
        otpUser
            .filter(t -> t.getOperationType().equals(OperationType.MFA))
            .filter(t -> t.getSecret().equals(inputOtp))
            .filter(
                t -> t.getCreatedAt().isAfter(timeProvider.getLocalDateTimeNow().minusMinutes(1)))
            .isPresent();

    if (validToken) {
      userService.resetFailedLoginAttempts(user);
    } else {
      userService.recordFailedLoginAttempt(user);
      userService.assertUserNotLocked(domainName, user);
      otpService.triggerOtp(user, OperationType.MFA);
    }

    return validToken;
  }
}
