package com.qbaaa.secure.auth.user.usecase;

import com.qbaaa.secure.auth.shared.config.time.TimeProvider;
import com.qbaaa.secure.auth.shared.exception.InputInvalidException;
import com.qbaaa.secure.auth.user.api.dto.ResetPasswordRequest;
import com.qbaaa.secure.auth.user.domain.enums.OperationType;
import com.qbaaa.secure.auth.user.domain.service.PasswordService;
import com.qbaaa.secure.auth.user.domain.service.UserService;
import com.qbaaa.secure.auth.user.infrastructure.entity.OtpEntity;
import com.qbaaa.secure.auth.user.infrastructure.entity.UserEntity;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResetPasswordUseCase {

  private final UserService userService;
  private final PasswordService passwordService;
  private final TimeProvider timeProvider;

  @Transactional
  public void execute(final String domainName, final ResetPasswordRequest passwordRequest) {
    var user =
        findUser(domainName, passwordRequest.username())
            .orElseThrow(() -> new BadCredentialsException("Invalid token!"));

    try {
      if (!passwordRequest.password().equals(passwordRequest.confirmPassword())) {
        throw new InputInvalidException("Passwords don't match");
      }

      if (!verifyOtp(passwordRequest.otp(), user.getOtp())) {
        throw new BadCredentialsException("Invalid token!");
      }

      passwordService.validatePassword(passwordRequest.password());
      passwordService.updatePassword(passwordRequest.password(), user.getPassword());
    } finally {
      userService.cleanOtp(user);
    }
  }

  private Optional<UserEntity> findUser(final String domainName, final String username) {
    return userService.findUserInDomain(domainName, username);
  }

  private boolean verifyOtp(String inputOtp, Optional<OtpEntity> otp) {

    return otp.filter(t -> t.getOperationType().equals(OperationType.RESET_PASSWORD))
        .filter(t -> t.getSecret().equals(inputOtp))
        .filter(t -> t.getCreatedAt().isAfter(timeProvider.getLocalDateTimeNow().minusMinutes(1)))
        .isPresent();
  }
}
