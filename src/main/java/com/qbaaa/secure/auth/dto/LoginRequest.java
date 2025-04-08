package com.qbaaa.secure.auth.dto;

import com.qbaaa.secure.auth.enums.AuthType;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class LoginRequest extends AuthRequest {

  private final String username;
  private final String password;

  public LoginRequest(String username, String password) {
    super(AuthType.PASSWORD);
    this.username = username;
    this.password = password;
  }
}
