package com.qbaaa.secure.auth.repository;

import com.qbaaa.secure.auth.entity.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SessionRepositoryTest extends JpaRepository<SessionEntity, Long> {

    @Query("""
                select count(s)
                from SessionEntity s
                where s.user.username = :username
                """)
    long countByUsername(String username);
}
