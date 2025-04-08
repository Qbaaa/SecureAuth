package com.qbaaa.secure.auth.dto;

import com.qbaaa.secure.auth.enums.AuthType;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class RefreshTokenRequest extends AuthRequest {

  String token;

  public RefreshTokenRequest(String token) {
    super(AuthType.REFRESH_TOKEN);
    this.token = token;
  }
}
