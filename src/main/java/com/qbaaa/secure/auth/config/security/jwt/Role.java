package com.qbaaa.secure.auth.config.security.jwt;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Role {
  ADMIN("ADMIN");

  private final String name;
}
