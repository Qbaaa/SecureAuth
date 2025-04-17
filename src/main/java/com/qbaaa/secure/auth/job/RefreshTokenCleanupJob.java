package com.qbaaa.secure.auth.job;

import com.qbaaa.secure.auth.config.time.TimeProvider;
import com.qbaaa.secure.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenCleanupJob {

  private final RefreshTokenRepository refreshTokenRepository;
  private final TimeProvider timeProvider;

  @Scheduled(cron = "${secureauth.job.cron.refreshTokenCleanup}")
  @SchedulerLock(name = "RefreshTokenCleanupJob_deleteExpiredTokens",
          lockAtLeastFor = "PT30M", lockAtMostFor = "PT35M")
  @Transactional
  public void deleteExpiredTokens() {
    var now = timeProvider.getLocalDateTimeNow();
    var deleteToken = refreshTokenRepository.deleteByExpiresAtLessThan(now);
    log.info("Deleted refresh token, number of deleted: {}", deleteToken);
  }
}
