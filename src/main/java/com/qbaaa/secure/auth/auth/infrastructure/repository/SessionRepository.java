package com.qbaaa.secure.auth.auth.infrastructure.repository;

import com.qbaaa.secure.auth.auth.infrastructure.entity.SessionEntity;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface SessionRepository extends JpaRepository<SessionEntity, Long> {

  @Query(
      """
                select (count(s) > 0)
                from SessionEntity s
                where s.sessionToken = :sessionToken
                and s.expiresAt >= :expiresAt
                """)
  boolean existsActiveSession(UUID sessionToken, LocalDateTime expiresAt);

  @Transactional
  @Modifying
  @Query(
      """
                delete
                from SessionEntity s
                where s.sessionToken = :sessionToken
                """)
  int deleteBySessionToken(UUID sessionToken);

  @Transactional
  @Modifying
  @Query(
      """
               delete
               from SessionEntity s
               where s.expiresAt < :expiresAt
               """)
  int deleteByExpiresAtLessThan(LocalDateTime expiresAt);
}
