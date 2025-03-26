package com.qbaaa.secure.auth.repository;

import com.qbaaa.secure.auth.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
}
