package com.qbaaa.secure.auth.domain.usecase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qbaaa.secure.auth.auth.infrastructure.mapper.DomainImportMapper;
import com.qbaaa.secure.auth.domain.domian.service.DomainService;
import com.qbaaa.secure.auth.domain.domian.service.KeyService;
import com.qbaaa.secure.auth.domain.infrastructure.dto.DomainTransferDto;
import com.qbaaa.secure.auth.shared.exception.DomainExistsException;
import com.qbaaa.secure.auth.shared.exception.DomainImportException;
import com.qbaaa.secure.auth.user.domain.service.RoleService;
import com.qbaaa.secure.auth.user.domain.service.UserService;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class DomainImportUseCase {

  private final DomainService domainService;
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
    if (domainService.existsByName(domainDto.name())) {
      throw new DomainExistsException("Domain already exists " + domainDto.name());
    }

    var domainEntity = domainService.save(domainImportMapper.mapDomainEntity(domainDto));
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
