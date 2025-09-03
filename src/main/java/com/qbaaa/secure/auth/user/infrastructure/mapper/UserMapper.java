package com.qbaaa.secure.auth.user.infrastructure.mapper;

import com.qbaaa.secure.auth.domain.infrastructure.dto.UserTransferDto;
import com.qbaaa.secure.auth.domain.infrastructure.entity.DomainEntity;
import com.qbaaa.secure.auth.user.api.dto.RegisterRequest;
import com.qbaaa.secure.auth.user.infrastructure.entity.RoleEntity;
import com.qbaaa.secure.auth.user.infrastructure.entity.UserEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "password", ignore = true)
  @Mapping(target = "refreshToken", ignore = true)
  @Mapping(target = "emailVerificationToken", ignore = true)
  @Mapping(target = "sessions", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "failedLoginAttempts", constant = "0")
  @Mapping(target = "lastFailedLoginTime", ignore = true)
  @Mapping(target = "domain", source = "domain")
  @Mapping(target = "roles", source = "roles")
  UserEntity mapUserEntity(
      UserTransferDto userTransferDto, DomainEntity domain, List<RoleEntity> roles);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "password", ignore = true)
  @Mapping(target = "refreshToken", ignore = true)
  @Mapping(target = "emailVerificationToken", ignore = true)
  @Mapping(target = "sessions", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "failedLoginAttempts", constant = "0")
  @Mapping(target = "lastFailedLoginTime", ignore = true)
  @Mapping(target = "isActive", expression = "java(!domain.getIsEnabledVerifiedEmail())")
  @Mapping(target = "domain", source = "domain")
  @Mapping(target = "roles", source = "roles")
  UserEntity mapUserEntity(
      RegisterRequest registerRequest, DomainEntity domain, List<RoleEntity> roles);
}
