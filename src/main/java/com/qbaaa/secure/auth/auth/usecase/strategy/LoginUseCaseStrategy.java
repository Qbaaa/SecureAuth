package com.qbaaa.secure.auth.auth.usecase.strategy;

import com.qbaaa.secure.auth.auth.api.dto.AuthRequest;
import com.qbaaa.secure.auth.auth.api.dto.AuthResponse;
import com.qbaaa.secure.auth.auth.api.dto.LoginRequest;
import com.qbaaa.secure.auth.auth.api.dto.MfaResponse;
import com.qbaaa.secure.auth.auth.domian.service.AuthTokenService;
import com.qbaaa.secure.auth.multifactorauth.domain.MfaProvider;
import com.qbaaa.secure.auth.shared.exception.UserNoActiveAccount;
import com.qbaaa.secure.auth.shared.security.CustomUsernamePasswordAuthenticationToken;
import com.qbaaa.secure.auth.shared.security.jwt.JwtService;
import com.qbaaa.secure.auth.user.domain.enums.OperationType;
import com.qbaaa.secure.auth.user.domain.service.OtpService;
import com.qbaaa.secure.auth.user.infrastructure.entity.UserEntity;
import java.util.List;
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
  private final AuthTokenService authTokenService;
  private final JwtService jwtService;
  private final OtpService otpService;
  private final List<MfaProvider> mfaProviders;

  @Transactional
  public AuthResponse authenticate(String domainName, String baseUrl, AuthRequest request) {
    var login = (LoginRequest) request;

    Authentication authentication =
        authenticationManager.authenticate(
            CustomUsernamePasswordAuthenticationToken.unauthenticated(
                domainName, login.getUsername(), login.getPassword()));

    var user = (UserEntity) authentication.getPrincipal();
    validateUserIsActive(domainName, user);

    if (isMfaEnabled(user)) {
      String pendingToken = jwtService.createMfaToken(baseUrl, domainName, user.getUsername());
      otpService.triggerOtp(user, OperationType.MFA);
      return new MfaResponse("MFA_REQUIRED", pendingToken);
    }

    return authTokenService.createToken(baseUrl, domainName, user);
  }

  private void validateUserIsActive(String domainName, UserEntity user) {
    if (BooleanUtils.isFalse(user.getIsActive())) {
      throw new UserNoActiveAccount(user.getUsername(), domainName);
    }
  }

  private boolean isMfaEnabled(UserEntity user) {
    return user.getIsMultifactorAuthEnabled();
  }
}
