package com.qbaaa.secure.auth.repository;

import com.qbaaa.secure.auth.entity.EmailVerificationTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface EmailTokenRepository extends JpaRepository<EmailVerificationTokenEntity, Long> {

  boolean existsByToken(String token);

  @Transactional
  @Modifying
  @Query(
      """
                    delete
                    from EmailVerificationTokenEntity e
                    where e.token = :token
                    """)
  int deleteByToken(String token);
}
