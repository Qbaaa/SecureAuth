package com.qbaaa.secure.auth.config.time;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class FakeTimeProviderConfig {

  @Bean
  @Primary
  public FakeTimeProvider fakeTimeProvider() {
    return new FakeTimeProvider();
  }
}
