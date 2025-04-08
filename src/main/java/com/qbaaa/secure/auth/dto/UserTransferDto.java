package com.qbaaa.secure.auth.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record UserTransferDto(
    @NotEmpty String username,
    @NotEmpty String email,
    Boolean isActive,
    PasswordTransferDto password,
    List<RoleTransferDto> roles) {
  @JsonCreator
  public UserTransferDto(
      @JsonProperty("username") String username,
      @JsonProperty("email") String email,
      @JsonProperty("isActive") Boolean isActive,
      @JsonProperty("password") PasswordTransferDto password,
      @JsonProperty("roles") List<RoleTransferDto> roles) {
    this.username = username;
    this.email = email;
    this.isActive = isActive != null ? isActive : Boolean.FALSE;
    this.password = password;
    this.roles = roles;
  }
}
