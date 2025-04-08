package com.qbaaa.secure.auth.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;

public record RoleTransferDto(@NotEmpty String name, String description, Boolean isDefault) {
  @JsonCreator
  public RoleTransferDto(
      @JsonProperty("name") String name,
      @JsonProperty("description") String description,
      @JsonProperty("isDefault") Boolean isDefault) {
    this.name = name;
    this.description = description;
    this.isDefault = isDefault != null ? isDefault : Boolean.FALSE;
  }
}
