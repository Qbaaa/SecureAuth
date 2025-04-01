package com.qbaaa.secure.auth.repository;

import com.qbaaa.secure.auth.entity.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<SessionEntity, Long> {

    @Query("""
                select (count(s) > 0)
                from SessionEntity s
                where s.sessionToken = :sessionToken
                and s.expiresAt >= :expiresAt
                """)
    boolean existsActiveSession(UUID sessionToken, LocalDateTime expiresAt);

    @Transactional
    @Modifying
    @Query("""
                delete
                from SessionEntity s
                where s.sessionToken = :sessionToken
                """)
    int deleteBySessionToken(UUID sessionToken);

}
