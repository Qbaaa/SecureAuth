package com.qbaaa.secure.auth.config.time;

import java.time.Instant;
import java.time.LocalDateTime;

public interface TimeProvider {

    Instant getTimestamp();

    LocalDateTime getLocalDateTimeNow();

}
