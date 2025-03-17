package com.qbaaa.secure.auth.repository;

import com.qbaaa.secure.auth.entity.PasswordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordRepository extends JpaRepository<PasswordEntity, Long> {
}
