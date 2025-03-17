package com.qbaaa.secure.auth.repository;

import com.qbaaa.secure.auth.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    boolean existsByName(String name);
}
