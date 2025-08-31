package com.qbaaa.secure.auth.repository;

import com.qbaaa.secure.auth.user.infrastructure.entity.EmailVerificationTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailTokenRepositoryTest
    extends JpaRepository<EmailVerificationTokenEntity, Long> {}
