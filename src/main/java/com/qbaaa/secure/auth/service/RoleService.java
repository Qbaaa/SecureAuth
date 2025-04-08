package com.qbaaa.secure.auth.service;

import com.qbaaa.secure.auth.dto.RoleTransferDto;
import com.qbaaa.secure.auth.entity.DomainEntity;
import com.qbaaa.secure.auth.entity.RoleEntity;
import com.qbaaa.secure.auth.exception.RoleAlreadyExistsException;
import com.qbaaa.secure.auth.mapper.RoleImportMapper;
import com.qbaaa.secure.auth.repository.RoleRepository;
import java.util.LinkedList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleService {

  private final RoleRepository roleRepository;
  private final RoleImportMapper roleImportMapper;

  public List<RoleEntity> assignRoleToDomain(
      DomainEntity domain, List<RoleTransferDto> rolesImport) {

    var roles = new LinkedList<RoleEntity>();
    rolesImport.forEach(
        roleImport -> {
          if (roleRepository.existsRole(domain.getName(), roleImport.name())) {
            throw new RoleAlreadyExistsException(
                String.format(
                    "Role already exists %s in domainName: %s",
                    roleImport.name(), domain.getName()));
          }
          var roleEntity = roleRepository.save(roleImportMapper.mapRoleEntity(roleImport, domain));
          roles.add(roleEntity);
        });
    return roles;
  }

  List<RoleEntity> getRolesToRegisterUser(String domainName) {
    return roleRepository.findByDomainNameAndIsDefault(domainName, true);
  }
}
