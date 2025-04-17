package com.qbaaa.secure.auth.job;

import com.qbaaa.secure.auth.config.time.TimeProvider;
import com.qbaaa.secure.auth.repository.EmailTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailTokenCleanupJob {

  private final EmailTokenRepository emailTokenRepository;
  private final TimeProvider timeProvider;

  @Scheduled(cron = "${secureauth.job.cron.emailTokenCleanup}")
  @Transactional
  public void deleteExpiredTokens() {
    var now = timeProvider.getLocalDateTimeNow();
    var deleteToken = emailTokenRepository.deleteByExpiresAtLessThan(now);
    log.info("Deleted email token, number of deleted: {}", deleteToken);
  }
}
