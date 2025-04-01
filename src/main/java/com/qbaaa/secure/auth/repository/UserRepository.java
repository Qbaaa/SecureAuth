package com.qbaaa.secure.auth.repository;

import com.qbaaa.secure.auth.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @Query("""
                select (count(u) > 0)
                from UserEntity u
                where u.domain.name = :domainName
                and (u.username = :username or u.email = :email)
               """)
    boolean existsUserInDomain(String domainName, String username, String email);

    @Query("""
                select u
                from UserEntity u
                join fetch u.roles r
                where u.domain.name = :domainName
                and u.username = :username
                """)
    Optional<UserEntity> findUserInDomain(String domainName, String username);

    @Query("""
                select u
                from UserEntity u
                left join fetch u.roles r
                inner join u.sessions sessions
                where sessions.sessionToken = :sessionToken
                """)
    Optional<UserEntity> findBySessions(UUID sessionToken);


}
