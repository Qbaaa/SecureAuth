package com.qbaaa.secure.auth.repository;

import com.qbaaa.secure.auth.entity.UserEntity;
import com.qbaaa.secure.auth.projection.UserProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByUsernameOrEmail(String username, String email);

    boolean existsByUsername(String username);

    @Query("""
                select u.username as username, u.email as email, u.password as password, r.name as role
                from UserEntity u
                join u.password p
                join u.roles r
                where u.username = :username
                        """)
    Optional<UserProjection> findByUsername(String username);

}
