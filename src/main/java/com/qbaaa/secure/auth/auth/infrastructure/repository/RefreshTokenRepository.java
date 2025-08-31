package com.qbaaa.secure.auth.auth.infrastructure.repository;

import com.qbaaa.secure.auth.auth.infrastructure.entity.RefreshTokenEntity;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

  @Transactional
  @Modifying
  @Query(
      """
                delete
                from RefreshTokenEntity r
                where r.token = :token
                """)
  int deleteByToken(String token);

  @Query(
      """
                select (count(r) > 0)
                from RefreshTokenEntity r
                where r.token = :token
                """)
  boolean existsRefreshToken(String token);

  @Transactional
  @Modifying
  @Query(
      """
                delete
                from RefreshTokenEntity r
                where r.expiresAt < :expiresAt
                """)
  int deleteByExpiresAtLessThan(LocalDateTime expiresAt);
}
