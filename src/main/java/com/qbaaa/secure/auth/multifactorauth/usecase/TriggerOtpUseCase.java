package com.qbaaa.secure.auth.multifactorauth.usecase;

import com.qbaaa.secure.auth.multifactorauth.api.dto.OperationRequest;
import com.qbaaa.secure.auth.multifactorauth.domain.enums.OperationPublicType;
import com.qbaaa.secure.auth.user.domain.enums.OperationType;
import com.qbaaa.secure.auth.user.domain.service.OtpService;
import com.qbaaa.secure.auth.user.domain.service.UserService;
import com.qbaaa.secure.auth.user.infrastructure.entity.UserEntity;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TriggerOtpUseCase {

  private final UserService userService;
  private final OtpService otpService;

  public String execution(final String domainName, final OperationRequest operationRequest) {

    findUser(domainName, operationRequest.username())
        .ifPresent(
            user -> {
              userService.cleanOtp(user);
              otpService.triggerOtp(user, convert(operationRequest.operation()));
            });

    return "Send OTP";
  }

  private Optional<UserEntity> findUser(final String domainName, final String username) {
    return userService.findUserInDomain(domainName, username);
  }

  private OperationType convert(OperationPublicType operationPublic) {
    return OperationType.valueOf(operationPublic.name());
  }
}
