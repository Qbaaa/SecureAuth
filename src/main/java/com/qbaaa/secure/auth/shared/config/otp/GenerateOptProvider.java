package com.qbaaa.secure.auth.shared.config.otp;

import com.qbaaa.secure.auth.shared.util.OtpUtils;
import org.springframework.stereotype.Component;

@Component
public class GenerateOptProvider implements OtpProvider {
  @Override
  public String generateSecret() {
    return OtpUtils.generateSecret();
  }
}
