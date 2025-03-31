package com.qbaaa.secure.auth.repository;

import com.qbaaa.secure.auth.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    @Query("""
            select (count(r) > 0)
            from RoleEntity r
            where r.domain.name = :domainName
            and r.name = :name
            """)
    boolean existsRole(String domainName, String name);
}
