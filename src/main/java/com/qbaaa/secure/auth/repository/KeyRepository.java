package com.qbaaa.secure.auth.repository;

import com.qbaaa.secure.auth.entity.KeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface KeyRepository extends JpaRepository<KeyEntity, Long> {

    @Query("""
                select k.privateKey
                from KeyEntity k
                where k.domain.name = :name
                """)
    Optional<String> findPrivateKeyByDomainName(String name);

    @Query("""
                select k.publicKey
                from KeyEntity k
                where k.domain.name = :name
                """)
    Optional<String> findPublicKeyByDomainName(String name);
}
