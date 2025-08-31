package com.qbaaa.secure.auth.user.infrastructure.repository;

import com.qbaaa.secure.auth.user.infrastructure.entity.PasswordEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PasswordRepository extends JpaRepository<PasswordEntity, Long> {

  @Query(
      """
                SELECT p.password
                FROM PasswordEntity p
                WHERE p.user.username = :username
                """)
  Optional<String> getPasswordByUsername(String username);
}
