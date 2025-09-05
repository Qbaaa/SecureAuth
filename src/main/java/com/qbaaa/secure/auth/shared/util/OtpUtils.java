package com.qbaaa.secure.auth.shared.util;

import lombok.experimental.UtilityClass;
import org.springframework.security.crypto.keygen.KeyGenerators;

@UtilityClass
public class OtpUtils {

  public static String generateSecret() {
    return KeyGenerators.string().generateKey();
  }
}
