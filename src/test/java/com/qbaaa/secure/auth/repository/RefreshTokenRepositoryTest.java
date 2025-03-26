package com.qbaaa.secure.auth.repository;

import com.qbaaa.secure.auth.entity.RefreshTokenEntity;
import com.qbaaa.secure.auth.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RefreshTokenRepositoryTest extends JpaRepository<RefreshTokenEntity, Long> {

    @Query("""
                select count(r)
                from RefreshTokenEntity r
                where r.user.username = :username
                """)
    long countByUsername(String username);

    String user(UserEntity user);
}
