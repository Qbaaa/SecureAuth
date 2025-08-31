package com.qbaaa.secure.auth.domain.domian.service;

import com.qbaaa.secure.auth.domain.infrastructure.entity.DomainEntity;
import com.qbaaa.secure.auth.domain.infrastructure.projection.DomainConfigValidityProjection;
import com.qbaaa.secure.auth.domain.infrastructure.repository.DomainRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DomainService {

  private final DomainRepository domainRepository;

  public DomainConfigValidityProjection getDomainConfigValidity(String domainName) {
    return domainRepository
        .findConfigValidityByName(domainName)
        .orElseThrow(() -> new EntityNotFoundException(domainName));
  }

  public DomainEntity getDomain(String domainName) {
    return domainRepository
        .findByName(domainName)
        .orElseThrow(() -> new EntityNotFoundException(domainName));
  }

  public boolean existsByName(String domainName) {
    return domainRepository.existsByName(domainName);
  }

  public DomainEntity save(DomainEntity domainEntity) {
    return domainRepository.save(domainEntity);
  }
}
