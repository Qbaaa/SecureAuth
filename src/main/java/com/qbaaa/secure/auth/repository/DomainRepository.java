package com.qbaaa.secure.auth.repository;

import com.qbaaa.secure.auth.entity.DomainEntity;
import com.qbaaa.secure.auth.projection.DomainConfigValidityProjection;
import com.qbaaa.secure.auth.projection.UserProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface DomainRepository extends JpaRepository<DomainEntity, Long> {

    boolean existsByName(String name);

    @Query("""
                select d.accessTokenValidity as accessTokenValidity, d.sessionValidity as sessionValidity,
                                d.refreshTokenValidity as refreshTokenValidity, d.emailTokenValidity as emailTokenValidity
                from DomainEntity d
                where d.name = :domainName
                """)
    Optional<DomainConfigValidityProjection> findConfigValidityByName(String domainName);
}
