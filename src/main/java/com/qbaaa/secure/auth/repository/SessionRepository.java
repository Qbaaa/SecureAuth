package com.qbaaa.secure.auth.repository;

import com.qbaaa.secure.auth.entity.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<SessionEntity, Long> {
}
