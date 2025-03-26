package com.qbaaa.secure.auth.repository;

import com.qbaaa.secure.auth.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByUsernameOrEmail(String username, String email);

    boolean existsByUsername(String username);

    @Query("""
                select u
                from UserEntity u
                join fetch u.roles r
                where u.username = :username
                """)
    Optional<UserEntity> findByUsername(String username);

}
