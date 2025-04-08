package com.qbaaa.secure.auth.config.time;

import java.time.Instant;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FakeTimeProvider implements TimeProvider {

  private LocalDateTime fixedDateTime = LocalDateTime.now();
  private Instant fixedTimestamp = Instant.now();

  public void setLocalDateTime(LocalDateTime newTime) {
    this.fixedDateTime = newTime;
    log.info("Set new time: {}", this.fixedDateTime);
  }

  public void setInstance(Instant newTime) {
    this.fixedTimestamp = newTime;
    log.info("Set new time: {}", this.fixedTimestamp);
  }

  @Override
  public Instant getTimestamp() {
    return fixedTimestamp;
  }

  @Override
  public LocalDateTime getLocalDateTimeNow() {
    return fixedDateTime;
  }
}
