package com.qbaaa.secure.auth.auth.domian.service;

import com.qbaaa.secure.auth.auth.infrastructure.entity.RefreshTokenEntity;
import com.qbaaa.secure.auth.auth.infrastructure.repository.RefreshTokenRepository;
import com.qbaaa.secure.auth.shared.config.time.TimeProvider;
import com.qbaaa.secure.auth.user.infrastructure.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;
  private final TimeProvider timeProvider;

  public void createRefreshToken(UserEntity user, Integer refreshTokenValidity, String token) {
    var dateTime = timeProvider.getLocalDateTimeNow();
    var refreshToken = new RefreshTokenEntity();
    refreshToken.setToken(token);
    refreshToken.setUser(user);
    refreshToken.setCreatedAt(dateTime);
    refreshToken.setExpiresAt(dateTime.plusSeconds(refreshTokenValidity));
    refreshTokenRepository.save(refreshToken);
  }

  public void deleteRefreshToken(String token) {
    var deleteToken = refreshTokenRepository.deleteByToken(token);
    if (deleteToken < 0) {
      log.warn("Failed to delete refresh token");
    }
    log.info("Successfully deleted refresh token, number of deleted: {}", deleteToken);
  }

  public boolean existsRefreshToken(String token) {
    return refreshTokenRepository.existsRefreshToken(token);
  }
}
