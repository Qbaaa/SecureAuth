package com.qbaaa.secure.auth.multifactorauth.infrastructure.provider;

import com.qbaaa.secure.auth.multifactorauth.domain.MfaProvider;
import com.qbaaa.secure.auth.user.domain.enums.MfaType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SmsProvider implements MfaProvider {
  @Override
  public void sendOtp(String recipient, String otp) {
    log.info("sendOtp: " + otp);
  }

  @Override
  public boolean supports(MfaType type) {
    return type == MfaType.SMS;
  }
}
