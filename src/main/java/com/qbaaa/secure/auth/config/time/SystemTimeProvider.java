package com.qbaaa.secure.auth.config.time;

import java.time.Instant;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class SystemTimeProvider implements TimeProvider {

  @Override
  public Instant getTimestamp() {
    return Instant.now();
  }

  @Override
  public LocalDateTime getLocalDateTimeNow() {
    return LocalDateTime.now();
  }
}
