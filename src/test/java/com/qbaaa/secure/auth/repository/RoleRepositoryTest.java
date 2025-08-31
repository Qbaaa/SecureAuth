package com.qbaaa.secure.auth.repository;

import com.qbaaa.secure.auth.user.infrastructure.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RoleRepositoryTest extends JpaRepository<RoleEntity, Long> {

  @Query(
      """
                select count(r)
                from RoleEntity r
                where r.domain.name = :domainName
                """)
  long countByDomainName(String domainName);
}
