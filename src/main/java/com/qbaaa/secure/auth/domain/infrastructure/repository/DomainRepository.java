package com.qbaaa.secure.auth.domain.infrastructure.repository;

import com.qbaaa.secure.auth.domain.infrastructure.entity.DomainEntity;
import com.qbaaa.secure.auth.domain.infrastructure.projection.DomainConfigValidityProjection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DomainRepository extends JpaRepository<DomainEntity, Long> {

  boolean existsByName(String name);

  @Query(
      """
                select d.accessTokenValidity as accessTokenValidity, d.sessionValidity as sessionValidity,
                                d.refreshTokenValidity as refreshTokenValidity, d.emailTokenValidity as emailTokenValidity
                from DomainEntity d
                where d.name = :domainName
                """)
  Optional<DomainConfigValidityProjection> findConfigValidityByName(String domainName);

  Optional<DomainEntity> findByName(String name);
}
