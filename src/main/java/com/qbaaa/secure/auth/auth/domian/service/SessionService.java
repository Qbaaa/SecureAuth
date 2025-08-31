package com.qbaaa.secure.auth.auth.domian.service;

import com.qbaaa.secure.auth.auth.infrastructure.entity.SessionEntity;
import com.qbaaa.secure.auth.auth.infrastructure.repository.SessionRepository;
import com.qbaaa.secure.auth.shared.config.time.TimeProvider;
import com.qbaaa.secure.auth.user.infrastructure.entity.UserEntity;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

  private final SessionRepository sessionRepository;
  private final TimeProvider timeProvider;

  public SessionEntity createSession(UserEntity user, Integer sessionValidity) {
    var session = new SessionEntity();
    var dateTime = timeProvider.getLocalDateTimeNow();
    session.setSessionToken(UUID.randomUUID());
    session.setUser(user);
    session.setCreatedAt(dateTime);
    session.setExpiresAt(dateTime.plusSeconds(sessionValidity));
    return sessionRepository.save(session);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public boolean verify(UUID sessionToken) {
    if (sessionRepository.existsActiveSession(sessionToken, LocalDateTime.now())) {
      return true;
    }
    delete(sessionToken);
    return false;
  }

  public void delete(UUID sessionToken) {
    var deleteNumberSession = sessionRepository.deleteBySessionToken(sessionToken);
    if (deleteNumberSession < 0) {
      log.warn("Failed to delete session");
    }
    log.info("Successfully deleted session, number of deleted: {}", deleteNumberSession);
  }
}
