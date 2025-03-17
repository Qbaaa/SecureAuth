package com.qbaaa.secure.auth.service;

import com.qbaaa.secure.auth.dto.UserTransferDto;
import com.qbaaa.secure.auth.entity.DomainEntity;
import com.qbaaa.secure.auth.entity.RoleEntity;
import com.qbaaa.secure.auth.exception.UserAlreadyException;
import com.qbaaa.secure.auth.mapper.UserImportMapper;
import com.qbaaa.secure.auth.projection.UserProjection;
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
            var userRoles = userImport.roles();
            var assignRolesToUser = rolesDomain.stream()
                    .filter(userRoles::contains)
                    .toList();
            if (userRepository.existsByUsernameOrEmail(userImport.username(), userImport.email())) {
                throw new UserAlreadyException(userImport.username(), userImport.email());
            }
            var userEntity = userRepository.save(userImportMapper.mapUserEntity(userImport, domain, assignRolesToUser));

            passwordService.saveToUser(userEntity, userImport.password());
        });

    }

    public Optional<UserProjection> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

}
