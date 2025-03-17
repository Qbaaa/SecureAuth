package com.qbaaa.secure.auth.service;

import com.qbaaa.secure.auth.dto.RoleTransferDto;
import com.qbaaa.secure.auth.entity.DomainEntity;
import com.qbaaa.secure.auth.entity.RoleEntity;
import com.qbaaa.secure.auth.exception.RoleAlreadyExists;
import com.qbaaa.secure.auth.mapper.RoleImportMapper;
import com.qbaaa.secure.auth.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final RoleImportMapper roleImportMapper;

    public List<RoleEntity> assignRoleToDomain(DomainEntity domain, List<RoleTransferDto> rolesImport) {

        var roles = new LinkedList<RoleEntity>();
        rolesImport.forEach(roleImport -> {
            if (roleRepository.existsByName(roleImport.name())) {
                throw new RoleAlreadyExists(roleImport.name());
            }
            var roleEntity = roleRepository.save(roleImportMapper.mapRoleEntity(roleImport, domain));
            roles.add(roleEntity);
        });
        return roles;
    }

}
