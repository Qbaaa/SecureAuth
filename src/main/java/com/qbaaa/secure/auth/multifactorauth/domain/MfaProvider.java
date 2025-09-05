package com.qbaaa.secure.auth.multifactorauth.domain;

import com.qbaaa.secure.auth.user.domain.enums.MfaType;

public interface MfaProvider {
  public void sendOtp(String recipient, String otp);

  boolean supports(MfaType type);
}
