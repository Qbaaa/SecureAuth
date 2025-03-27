package com.qbaaa.secure.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qbaaa.secure.auth.dto.DomainTransferDto;
import com.qbaaa.secure.auth.exception.DomainExistsException;
import com.qbaaa.secure.auth.exception.DomainImportException;
import com.qbaaa.secure.auth.mapper.DomainImportMapper;
import com.qbaaa.secure.auth.repository.DomainRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class DomainImportService {


    private final DomainRepository domainrepository;
    private final KeyService keyService;
    private final RoleService roleService;
    private final UserService userService;
    private final DomainImportMapper domainImportMapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public void importFileDomain(MultipartFile fileUpload) {
        try {
            var domainDto = convertFileToJson(fileUpload.getBytes());
            importJsonDomain(domainDto);

        } catch (Exception e) {
            throw new DomainImportException(e.getMessage());
        }

    }

    @Transactional
    public void importDomainStartApplication(DomainTransferDto domainTransferDto) {
        try {
            importJsonDomain(domainTransferDto);

        } catch (Exception e) {
            throw new DomainImportException(e.getMessage());
        }
    }


    private void importJsonDomain(DomainTransferDto domainDto) {
        if (domainrepository.existsByName(domainDto.name())) {
            throw new DomainExistsException(domainDto.name());
        }

        var domainEntity = domainrepository.save(domainImportMapper.mapDomainEntity(domainDto));
        keyService.generateKeyForDomain(domainEntity);

        var rolesDomainDto = domainDto.roles();
        var rolesEntity = roleService.assignRoleToDomain(domainEntity, rolesDomainDto);

        var usersDomainDto = domainDto.users();
        userService.assignUsersToDomain(domainEntity, usersDomainDto, rolesEntity);
    }

    private DomainTransferDto convertFileToJson(byte[] fileBytes) throws JsonProcessingException {
        var jsonContent = new String(fileBytes, StandardCharsets.UTF_8);
        return objectMapper.readValue(jsonContent, DomainTransferDto.class);
    }

}
