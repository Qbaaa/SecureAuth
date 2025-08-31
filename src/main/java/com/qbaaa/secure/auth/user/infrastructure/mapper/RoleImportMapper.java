package com.qbaaa.secure.auth.user.infrastructure.mapper;

import com.qbaaa.secure.auth.domain.infrastructure.dto.RoleTransferDto;
import com.qbaaa.secure.auth.domain.infrastructure.entity.DomainEntity;
import com.qbaaa.secure.auth.user.infrastructure.entity.RoleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RoleImportMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "name", source = "roleTransferDto.name")
  @Mapping(target = "users", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  RoleEntity mapRoleEntity(RoleTransferDto roleTransferDto, DomainEntity domain);
}
