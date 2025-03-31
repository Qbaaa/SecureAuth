package com.qbaaa.secure.auth.service;

import com.qbaaa.secure.auth.dto.RoleTransferDto;
import com.qbaaa.secure.auth.dto.UserTransferDto;
import com.qbaaa.secure.auth.entity.DomainEntity;
import com.qbaaa.secure.auth.entity.RoleEntity;
import com.qbaaa.secure.auth.entity.UserEntity;
import com.qbaaa.secure.auth.exception.UserAlreadyExistsException;
import com.qbaaa.secure.auth.mapper.UserImportMapper;
import com.qbaaa.secure.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final UserImportMapper userImportMapper;

    public void assignUsersToDomain(DomainEntity domain, List<UserTransferDto> usersImport, List<RoleEntity> rolesDomain) {

        usersImport.forEach(userImport -> {
            var userImportRoles = userImport.roles().stream().map(RoleTransferDto::name).toList();
            var assignRolesToUser = rolesDomain.stream()
                    .filter(roleDomain -> userImportRoles.contains(roleDomain.getName()))
                    .toList();
            if (userRepository.existsUserInDomain(domain.getName(), userImport.username(), userImport.email())) {
                throw new UserAlreadyExistsException(userImport.username(), userImport.email(), domain.getName());
            }
            var userEntity = userRepository.save(userImportMapper.mapUserEntity(userImport, domain, assignRolesToUser));

            passwordService.saveToUser(userEntity, userImport.password());
        });

    }

    public Optional<UserEntity> findUserInDomain(String domainName, String username) {
        return userRepository.findUserInDomain(domainName, username);
    }

}
