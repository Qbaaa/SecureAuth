package com.qbaaa.secure.auth.shared.config.security.jwt;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Role {
  ADMIN("ADMIN");

  private final String name;
}
