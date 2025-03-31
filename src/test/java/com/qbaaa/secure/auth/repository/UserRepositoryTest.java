package com.qbaaa.secure.auth.repository;

import com.qbaaa.secure.auth.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepositoryTest extends JpaRepository<UserEntity, Long> {

    @Query("""
                select count(u)
                from UserEntity u
                where u.domain.name = :domainName
                """)
    long countByDomainName(String domainName);

    @Query("""
                select u
                from UserEntity u
                left join fetch u.roles r
                where u.domain.name = :domainName
                and u.username = :username
                """)
    Optional<UserEntity> findByDomainNameAndUsername(String domainName, String username);

}
