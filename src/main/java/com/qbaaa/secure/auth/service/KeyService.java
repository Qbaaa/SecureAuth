package com.qbaaa.secure.auth.service;

import com.qbaaa.secure.auth.entity.DomainEntity;
import com.qbaaa.secure.auth.entity.KeyEntity;
import com.qbaaa.secure.auth.exception.GenerateKeyException;
import com.qbaaa.secure.auth.repository.KeyRepository;
import com.qbaaa.secure.auth.util.GenerateRsaKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class KeyService {

    private final KeyRepository keyRepository;

    public void generateKeyForDomain(DomainEntity domainEntity) {
        try {
            var keyPair = GenerateRsaKey.generateRsaKey();
            var key = KeyEntity.builder()
                    .algorithm(keyPair.getPublic().getAlgorithm())
                    .privateKey(Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()))
                    .publicKey(Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()))
                    .domain(domainEntity)
                    .build();

            keyRepository.save(key);
        } catch (NoSuchAlgorithmException e) {
            throw new GenerateKeyException(e.getMessage());
        }

    }

}
