package com.qbaaa.secure.auth.repository;

import com.qbaaa.secure.auth.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    @Transactional
    @Modifying
    @Query("""
                delete
                from RefreshTokenEntity r
                where r.token = :token
                """)
    int deleteByToken(String token);

    @Query("""
                select (count(r) > 0)
                from RefreshTokenEntity r
                where r.token = :token
                """)
    boolean existsRefreshToken(String token);

}
