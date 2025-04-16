package com.qbaaa.secure.auth.service;

import com.qbaaa.secure.auth.config.time.TimeProvider;
import com.qbaaa.secure.auth.entity.EmailVerificationTokenEntity;
import com.qbaaa.secure.auth.entity.UserEntity;
import com.qbaaa.secure.auth.repository.EmailTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailTokenService {

  private final EmailTokenRepository emailTokenRepository;
  private final TimeProvider timeProvider;

  public void createEmailToken(UserEntity user, Integer emailTokenValidity, String token) {
    var dateTime = timeProvider.getLocalDateTimeNow();
    var emailToken = new EmailVerificationTokenEntity();
    emailToken.setToken(token);
    emailToken.setUser(user);
    emailToken.setCreatedAt(dateTime);
    emailToken.setExpiresAt(dateTime.plusSeconds(emailTokenValidity));
    emailTokenRepository.save(emailToken);
  }

  public boolean existsEmailToken(String token) {
    return emailTokenRepository.existsByToken(token);
  }

  public void deleteEmailToken(String token) {
    var emailToken = emailTokenRepository.deleteByToken(token);
    if (emailToken < 0) {
      log.warn("Failed to delete email token");
    }
    log.info("Successfully deleted email token, number of deleted: {}", emailToken);
  }
}
