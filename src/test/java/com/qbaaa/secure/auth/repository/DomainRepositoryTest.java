package com.qbaaa.secure.auth.repository;

import com.qbaaa.secure.auth.domain.infrastructure.entity.DomainEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DomainRepositoryTest extends JpaRepository<DomainEntity, Long> {}
