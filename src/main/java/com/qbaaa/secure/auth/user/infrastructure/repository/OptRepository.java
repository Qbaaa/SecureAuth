package com.qbaaa.secure.auth.user.infrastructure.repository;

import com.qbaaa.secure.auth.user.infrastructure.entity.OtpEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OptRepository extends JpaRepository<OtpEntity, Long> {}
