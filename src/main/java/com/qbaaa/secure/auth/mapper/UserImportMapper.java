package com.qbaaa.secure.auth.mapper;

import com.qbaaa.secure.auth.dto.UserTransferDto;
import com.qbaaa.secure.auth.entity.DomainEntity;
import com.qbaaa.secure.auth.entity.RoleEntity;
import com.qbaaa.secure.auth.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserImportMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    @Mapping(target = "emailVerificationToken", ignore = true)
    @Mapping(target = "sessions", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "roles", source = "roles")
    UserEntity mapUserEntity(UserTransferDto userTransferDto, DomainEntity domain, List<RoleEntity> roles);
}
