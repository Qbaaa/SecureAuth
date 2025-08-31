package com.qbaaa.secure.auth.domain.domian.service;

import com.qbaaa.secure.auth.domain.infrastructure.entity.DomainEntity;
import com.qbaaa.secure.auth.domain.infrastructure.entity.KeyEntity;
import com.qbaaa.secure.auth.domain.infrastructure.repository.KeyRepository;
import com.qbaaa.secure.auth.shared.exception.GenerateKeyException;
import com.qbaaa.secure.auth.shared.util.GenerateRsaKey;
import jakarta.persistence.EntityNotFoundException;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KeyService {

  private final KeyRepository keyRepository;

  public void generateKeyForDomain(DomainEntity domainEntity) {
    try {
      var keyPair = GenerateRsaKey.generateRsaKey();
      var key =
          KeyEntity.builder()
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

  public RSAPrivateKey getPrivateKey(String domainName) {
    try {
      var keyData =
          keyRepository
              .findPrivateKeyByDomainName(domainName)
              .orElseThrow(
                  () ->
                      new EntityNotFoundException(
                          "Private key not found for domain " + domainName));

      return GenerateRsaKey.getPrivateKey(keyData);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new GenerateKeyException(e.getMessage());
    }
  }

  public RSAPublicKey getPublicKey(String domainName) {
    try {
      var keyData =
          keyRepository
              .findPublicKeyByDomainName(domainName)
              .orElseThrow(
                  () ->
                      new EntityNotFoundException("Public key not found for domain " + domainName));

      return GenerateRsaKey.getPublicKey(keyData);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new GenerateKeyException(e.getMessage());
    }
  }
}
