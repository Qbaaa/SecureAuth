package com.qbaaa.secure.auth.auth.domian.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthType {
  PASSWORD("password"),
  REFRESH_TOKEN("refresh-token");

  private final String value;
}
