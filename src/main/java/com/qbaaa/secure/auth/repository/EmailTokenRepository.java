package com.qbaaa.secure.auth.repository;

import com.qbaaa.secure.auth.entity.EmailVerificationTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailTokenRepository extends JpaRepository<EmailVerificationTokenEntity, Long> {}
