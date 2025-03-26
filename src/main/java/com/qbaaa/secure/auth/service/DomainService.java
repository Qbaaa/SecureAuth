package com.qbaaa.secure.auth.service;

import com.qbaaa.secure.auth.projection.DomainConfigValidityProjection;
import com.qbaaa.secure.auth.repository.DomainRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DomainService {

    private final DomainRepository domainRepository;

    public DomainConfigValidityProjection getDomainConfigValidity(String domainName) {

        return domainRepository.findConfigValidityByName(domainName).orElseThrow(() ->
                new EntityNotFoundException(domainName));
    }
}
