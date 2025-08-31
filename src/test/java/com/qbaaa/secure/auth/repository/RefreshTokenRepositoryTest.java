package com.qbaaa.secure.auth.repository;

import com.qbaaa.secure.auth.auth.infrastructure.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RefreshTokenRepositoryTest extends JpaRepository<RefreshTokenEntity, Long> {

  @Query(
      """
                select count(r)
                from RefreshTokenEntity r
                where r.user.username = :username
                """)
  long countByUsername(String username);

  boolean existsByToken(String token);
}
