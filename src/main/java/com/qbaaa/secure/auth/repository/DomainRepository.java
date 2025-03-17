package com.qbaaa.secure.auth.repository;

import com.qbaaa.secure.auth.entity.DomainEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DomainRepository extends JpaRepository<DomainEntity, Long> {

    boolean existsByName(String name);
}
