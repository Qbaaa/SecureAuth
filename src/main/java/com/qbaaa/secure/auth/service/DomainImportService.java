package com.qbaaa.secure.auth.service;

import com.qbaaa.secure.auth.dto.DomainTransferDto;
import com.qbaaa.secure.auth.exception.DomainExistsException;
import com.qbaaa.secure.auth.exception.DomainImportException;
import com.qbaaa.secure.auth.mapper.DomainImportMapper;
import com.qbaaa.secure.auth.repository.DomainRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DomainImportService {


    private final DomainRepository domainrepository;
    private final KeyService keyService;
    private final RoleService roleService;
    private final UserService userService;
    private final DomainImportMapper domainImportMapper;

    @Transactional
    public void importDomain(DomainTransferDto domainDto) {
        try {
            if (domainrepository.existsByName(domainDto.name())) {
                throw new DomainExistsException(domainDto.name());
            }

            var domainEntity = domainrepository.save(domainImportMapper.mapDomainEntity(domainDto));
            keyService.generateKeyForDomain(domainEntity);

            var rolesDomainDto = domainDto.roles();
            var rolesEntity = roleService.assignRoleToDomain(domainEntity, rolesDomainDto);

            var usersDomainDto = domainDto.users();
            userService.assignUsersToDomain(domainEntity, usersDomainDto, rolesEntity);

        } catch (Exception e) {
            throw new DomainImportException(e.getMessage());
        }

    }

}
