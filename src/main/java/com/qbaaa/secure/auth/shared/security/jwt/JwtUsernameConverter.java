package com.qbaaa.secure.auth.shared.security.jwt;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class JwtUsernameConverter implements Converter<DecodedJWT, String> {

  private static final String CLAIM_USERNAME = "username";

  @Override
  public String convert(DecodedJWT jwt) {
    var username = jwt.getClaim(CLAIM_USERNAME).asString();

    if (username == null) {
      throw new IllegalStateException("Jwt requires username claim");
    }

    return username;
  }
}
