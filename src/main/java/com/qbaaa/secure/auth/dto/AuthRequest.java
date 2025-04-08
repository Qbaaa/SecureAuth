package com.qbaaa.secure.auth.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.qbaaa.secure.auth.enums.AuthType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = LoginRequest.class, name = "password"),
  @JsonSubTypes.Type(value = RefreshTokenRequest.class, name = "refresh-token")
})
@Getter
@RequiredArgsConstructor
public abstract class AuthRequest {

  @NotNull private final AuthType type;
}
