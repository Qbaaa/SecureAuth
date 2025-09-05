package com.qbaaa.secure.auth.shared.security.jwt;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Role {
  ADMIN("ADMIN"),
  PENDING_LOGIN_MFA("PENDING_LOGIN_MFA");

  private final String name;
}
