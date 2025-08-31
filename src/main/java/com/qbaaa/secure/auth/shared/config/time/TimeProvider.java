package com.qbaaa.secure.auth.shared.config.time;

import java.time.Instant;
import java.time.LocalDateTime;

public interface TimeProvider {

  Instant getTimestamp();

  LocalDateTime getLocalDateTimeNow();
}
