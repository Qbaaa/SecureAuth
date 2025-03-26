package com.qbaaa.secure.auth.service;

import com.qbaaa.secure.auth.entity.SessionEntity;
import com.qbaaa.secure.auth.entity.UserEntity;
import com.qbaaa.secure.auth.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionServer {

    private final SessionRepository sessionRepository;

    public SessionEntity createSession(UserEntity user, Integer sessionValidity) {
        var session = new SessionEntity();
        session.setSessionToken(UUID.randomUUID());
        session.setUser(user);
        session.setCreatedAt(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plusSeconds(sessionValidity));
        return sessionRepository.save(session);
    }
}
