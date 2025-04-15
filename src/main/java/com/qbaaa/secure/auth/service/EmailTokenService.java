package com.qbaaa.secure.auth.service;

import com.qbaaa.secure.auth.config.time.TimeProvider;
import com.qbaaa.secure.auth.entity.EmailVerificationTokenEntity;
import com.qbaaa.secure.auth.entity.UserEntity;
import com.qbaaa.secure.auth.repository.EmailTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
