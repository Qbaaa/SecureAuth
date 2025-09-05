package com.qbaaa.secure.auth.user.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum MfaType {
  EMAIL("EMAIL"),
  SMS("SMS");

  private final String type;
}
