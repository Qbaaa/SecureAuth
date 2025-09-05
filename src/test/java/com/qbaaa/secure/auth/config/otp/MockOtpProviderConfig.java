package com.qbaaa.secure.auth.config.otp;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class MockOtpProviderConfig {

  @Bean
  @Primary
  public MockOtpProvider mockOtpProvider() {
    return new MockOtpProvider();
  }
}
