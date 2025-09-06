package com.qbaaa.secure.auth.user.domain.service;

import com.qbaaa.secure.auth.auth.domian.config.AccountLockedProperties;
import com.qbaaa.secure.auth.domain.infrastructure.dto.PasswordTransferDto;
import com.qbaaa.secure.auth.domain.infrastructure.dto.RoleTransferDto;
import com.qbaaa.secure.auth.domain.infrastructure.dto.UserTransferDto;
import com.qbaaa.secure.auth.domain.infrastructure.entity.DomainEntity;
import com.qbaaa.secure.auth.shared.config.time.TimeProvider;
import com.qbaaa.secure.auth.shared.exception.AccountLockedException;
import com.qbaaa.secure.auth.shared.exception.EmailAlreadyExistsException;
import com.qbaaa.secure.auth.shared.exception.UserAlreadyExistsException;
import com.qbaaa.secure.auth.shared.exception.UsernameAlreadyExistsException;
import com.qbaaa.secure.auth.user.api.dto.RegisterRequest;
import com.qbaaa.secure.auth.user.infrastructure.entity.RoleEntity;
import com.qbaaa.secure.auth.user.infrastructure.entity.UserEntity;
import com.qbaaa.secure.auth.user.infrastructure.mapper.UserMapper;
import com.qbaaa.secure.auth.user.infrastructure.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordService passwordService;
  private final RoleService roleService;
  private final UserMapper userMapper;
  private final TimeProvider timeProvider;
  private final AccountLockedProperties accountLockedProperties;

  public Optional<UserEntity> findUserInDomain(String domainName, String username) {
    return userRepository.findUserInDomain(domainName, username);
  }

  public Optional<UserEntity> findUserBySession(UUID sessionToken) {
    return userRepository.findBySessions(sessionToken);
  }

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
    passwordService.validatePassword(registerRequest.password());

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

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void recordFailedLoginAttempt(UserEntity user) {

    user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
    user.setLastFailedLoginTime(timeProvider.getLocalDateTimeNow());
    user.setOtp(null);
    userRepository.save(user);
  }

  @Transactional
  public void resetFailedLoginAttempts(UserEntity user) {
    user.setFailedLoginAttempts(0);
    user.setLastFailedLoginTime(null);
    user.setOtp(null);
    userRepository.save(user);
  }

  public void assertUserNotLocked(String domainName, UserEntity user) {
    user.getLastFailedLoginTime()
        .ifPresent(
            lastFailedLoginTime -> {
              final Duration lockDuration = calculateNextLockDuration(user);
              final LocalDateTime lockExpiry = lastFailedLoginTime.plus(lockDuration);
              final LocalDateTime now = timeProvider.getLocalDateTimeNow();
              if (lockExpiry.isAfter(now)) {
                throw new AccountLockedException(domainName, user.getUsername());
              }
            });
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void cleanOtp(UserEntity user) {
    var managedUser =
        userRepository
            .findById(user.getId())
            .orElseThrow(() -> new IllegalStateException("User not found when cleaning OTP"));

    managedUser.setOtp(null);
  }

  private Duration calculateNextLockDuration(final UserEntity user) {
    if (user.getFailedLoginAttempts() < accountLockedProperties.getAttempt()) {
      return Duration.ZERO;
    }
    return Duration.ofMinutes(accountLockedProperties.getTime());
  }
}
