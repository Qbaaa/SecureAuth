package com.qbaaa.secure.auth.repository;

import com.qbaaa.secure.auth.entity.KeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeyRepository extends JpaRepository<KeyEntity, Long> {
}
