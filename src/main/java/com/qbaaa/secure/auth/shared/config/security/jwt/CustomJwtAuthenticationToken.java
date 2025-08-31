package com.qbaaa.secure.auth.shared.config.security.jwt;

import java.util.Collection;
import java.util.Collections;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class CustomJwtAuthenticationToken extends AbstractAuthenticationToken {

  @Getter private final Object principal;
  @Getter private final Object credentials;
  @Getter private final Object token;
  @Getter private final String issuer;

  private CustomJwtAuthenticationToken(Object token, String issuer) {
    super(Collections.emptyList());
    this.principal = token;
    this.credentials = token;
    this.token = token;
    this.issuer = issuer;
    super.setAuthenticated(false);
  }

  private CustomJwtAuthenticationToken(
      Object token, String issuer, Collection<? extends GrantedAuthority> authorities) {
    super(authorities);
    this.principal = token;
    this.credentials = token;
    this.token = token;
    this.issuer = issuer;
    super.setAuthenticated(true);
  }

  public static CustomJwtAuthenticationToken unauthenticated(Object token, String issuer) {
    return new CustomJwtAuthenticationToken(token, issuer);
  }

  public static CustomJwtAuthenticationToken authenticated(
      Object token, String issuer, Collection<? extends GrantedAuthority> authorities) {
    return new CustomJwtAuthenticationToken(token, issuer, authorities);
  }
}
