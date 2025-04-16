package com.qbaaa.secure.auth.service;

import com.qbaaa.secure.auth.dto.PasswordTransferDto;
import com.qbaaa.secure.auth.dto.RegisterRequest;
import com.qbaaa.secure.auth.dto.RoleTransferDto;
import com.qbaaa.secure.auth.dto.UserTransferDto;
import com.qbaaa.secure.auth.entity.DomainEntity;
import com.qbaaa.secure.auth.entity.RoleEntity;
import com.qbaaa.secure.auth.entity.UserEntity;
import com.qbaaa.secure.auth.exception.EmailAlreadyExistsException;
import com.qbaaa.secure.auth.exception.UserAlreadyExistsException;
import com.qbaaa.secure.auth.exception.UsernameAlreadyExistsException;
import com.qbaaa.secure.auth.mapper.UserMapper;
import com.qbaaa.secure.auth.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordService passwordService;
  private final RoleService roleService;
  private final UserMapper userMapper;

  public void assignUsersToDomain(
      DomainEntity domain, List<UserTransferDto> usersImport, List<RoleEntity> rolesDomain) {

    usersImport.forEach(
        userImport -> {
          var userImportRoles = userImport.roles().stream().map(RoleTransferDto::name).toList();
          var assignRolesToUser =
              rolesDomain.stream()
                  .filter(roleDomain -> userImportRoles.contains(roleDomain.getName()))
                  .toList();
          if (userRepository.existsUserInDomain(
              domain.getName(), userImport.username(), userImport.email())) {
            throw new UserAlreadyExistsException(
                userImport.username(), userImport.email(), domain.getName());
          }
          var userEntity =
              userRepository.save(userMapper.mapUserEntity(userImport, domain, assignRolesToUser));

          passwordService.saveToUser(userEntity, userImport.password());
        });
  }

  public Optional<UserEntity> findUserInDomain(String domainName, String username) {
    return userRepository.findUserInDomain(domainName, username);
  }

  public Optional<UserEntity> findUserBySession(UUID sessionToken) {
    return userRepository.findBySessions(sessionToken);
  }

  public UserEntity register(DomainEntity domain, RegisterRequest registerRequest) {
    if (Boolean.TRUE.equals(
        userRepository.existsByDomainNameAndUsername(
            domain.getName(), registerRequest.username()))) {
      throw new UsernameAlreadyExistsException(
          "Username is already in use. Please choose a different one.");
    }

    if (Boolean.TRUE.equals(
        userRepository.existsByDomainNameAndEmail(domain.getName(), registerRequest.email()))) {
      throw new EmailAlreadyExistsException(
          "Email is already in use. Please choose a different one.");
    }

    var assignRolesToUser = roleService.getRolesToRegisterUser(domain.getName());
    var userEntity =
        userRepository.save(userMapper.mapUserEntity(registerRequest, domain, assignRolesToUser));

    var passwordTransfer = new PasswordTransferDto(registerRequest.password());
    passwordService.saveToUser(userEntity, passwordTransfer);

    return userEntity;
  }

  public void activeAccount(String domainName, String username) {
    var user =
        userRepository
            .findUserInDomain(domainName, username)
            .orElseThrow(() -> new EntityNotFoundException("User not found in domain"));

    user.setIsActive(Boolean.TRUE);
    userRepository.save(user);
  }
}
