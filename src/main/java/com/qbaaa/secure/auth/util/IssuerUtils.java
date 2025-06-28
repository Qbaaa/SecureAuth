package com.qbaaa.secure.auth.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class IssuerUtils {

  public static String buildIssuer(String baseUrl, String domainName) {
    return String.format("%s/domains/%s", baseUrl, domainName);
  }
}
