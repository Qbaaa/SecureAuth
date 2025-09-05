package com.qbaaa.secure.auth.config.otp;

import com.qbaaa.secure.auth.shared.config.otp.OtpProvider;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MockOtpProvider implements OtpProvider {

  private String secretMock = "secret123456";

  @Override
  public String generateSecret() {
    return secretMock;
  }

  public void setSecretMock(String secretMock) {
    this.secretMock = secretMock;
    log.info("Set new secret: {}", this.secretMock);
  }
}
