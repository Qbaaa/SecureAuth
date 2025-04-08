package com.qbaaa.secure.auth.mapper;

import com.qbaaa.secure.auth.dto.DomainTransferDto;
import com.qbaaa.secure.auth.entity.DomainEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface DomainImportMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "users", ignore = true)
  @Mapping(target = "roles", ignore = true)
  @Mapping(target = "key", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  DomainEntity mapDomainEntity(DomainTransferDto domainImport);
}
