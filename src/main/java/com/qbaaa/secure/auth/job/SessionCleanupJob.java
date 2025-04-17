package com.qbaaa.secure.auth.job;

import com.qbaaa.secure.auth.config.time.TimeProvider;
import com.qbaaa.secure.auth.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SessionCleanupJob {

  private final SessionRepository sessionRepository;
  private final TimeProvider timeProvider;

  @Scheduled(cron = "${secureauth.job.cron.sessionCleanup}")
  @SchedulerLock(
      name = "SessionCleanupJob_deleteExpiredTokens",
      lockAtLeastFor = "PT30M",
      lockAtMostFor = "PT35M")
  @Transactional
  public void deleteExpiredTokens() {
    var now = timeProvider.getLocalDateTimeNow();
    var deleteSession = sessionRepository.deleteByExpiresAtLessThan(now);
    log.info("Deleted session, number of deleted: {}", deleteSession);
  }
}
