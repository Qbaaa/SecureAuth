package com.qbaaa.secure.auth.user.usecase;

import com.qbaaa.secure.auth.domain.domian.service.DomainService;
import com.qbaaa.secure.auth.domain.infrastructure.entity.DomainEntity;
import com.qbaaa.secure.auth.shared.event.AccountActiveEvent;
import com.qbaaa.secure.auth.shared.exception.RegisterException;
import com.qbaaa.secure.auth.shared.security.jwt.JwtService;
import com.qbaaa.secure.auth.user.api.dto.RegisterRequest;
import com.qbaaa.secure.auth.user.domain.service.EmailTokenService;
import com.qbaaa.secure.auth.user.domain.service.UserService;
import com.qbaaa.secure.auth.user.infrastructure.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterAccountUseCase {

  private final JwtService jwtService;
  private final DomainService domainService;
  private final UserService userService;
  private final ApplicationEventPublisher eventPublisher;
  private final EmailTokenService emailTokenService;

  @Transactional
  public String register(String baseUrl, String domainName, RegisterRequest registerRequest) {
    var domain = domainService.getDomain(domainName);
    validateRegisterEnabled(domain);
    var user = userService.register(domain, registerRequest);

    return Boolean.TRUE.equals(domain.getIsEnabledVerifiedEmail())
        ? sendVerificationEmail(baseUrl, domainName, domain, user)
        : "Created active account";
  }

  private void validateRegisterEnabled(DomainEntity domain) {
    if (Boolean.FALSE.equals(domain.getIsEnabledRegister())) {
      throw new RegisterException("Registration is disabled for this domain");
    }
  }

  private String sendVerificationEmail(
      String baseUrl, String domainName, DomainEntity domain, UserEntity user) {
    var token =
        jwtService.createActiveAccountToken(
            baseUrl, domainName, domain.getEmailTokenValidity(), user.getUsername());

    emailTokenService.createEmailToken(user, domain.getEmailTokenValidity(), token);

    eventPublisher.publishEvent(
        new AccountActiveEvent(baseUrl, domainName, user.getUsername(), user.getEmail(), token));

    return "Sent email with link activate account";
  }
}
