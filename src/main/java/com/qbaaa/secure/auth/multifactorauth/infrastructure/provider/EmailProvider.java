package com.qbaaa.secure.auth.multifactorauth.infrastructure.provider;

import com.qbaaa.secure.auth.multifactorauth.domain.MfaProvider;
import com.qbaaa.secure.auth.multifactorauth.domain.config.OtpLoginProperties;
import com.qbaaa.secure.auth.shared.email.EmailService;
import com.qbaaa.secure.auth.user.domain.enums.MfaType;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailProvider implements MfaProvider {

  private final EmailService emailService;
  private final OtpLoginProperties properties;

  @Override
  public void sendOtp(String recipient, String otp) {
    var templateInputMap = Map.of("secret", otp);
    emailService.send(
        properties.getSender(),
        recipient,
        "Your SecureAuth login code",
        properties.getTemplate(),
        templateInputMap);
  }

  @Override
  public boolean supports(MfaType type) {
    return type == MfaType.EMAIL;
  }
}
