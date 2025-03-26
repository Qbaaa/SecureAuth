package com.qbaaa.secure.auth.service;

import com.qbaaa.secure.auth.entity.RefreshTokenEntity;
import com.qbaaa.secure.auth.entity.UserEntity;
import com.qbaaa.secure.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public void createRefreshToken(UserEntity user, Integer refreshTokenValidity, String token) {
        var refreshToken = new RefreshTokenEntity();
        refreshToken.setToken(token);
        refreshToken.setUser(user);
        refreshToken.setCreatedAt(LocalDateTime.now());
        refreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(refreshTokenValidity));
        refreshTokenRepository.save(refreshToken);
    }
}
