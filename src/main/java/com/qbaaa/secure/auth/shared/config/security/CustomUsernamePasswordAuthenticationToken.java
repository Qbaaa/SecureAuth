package com.qbaaa.secure.auth.shared.config.security;

import java.util.Collection;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

public class CustomUsernamePasswordAuthenticationToken extends AbstractAuthenticationToken {
  private final Object principal;
  private Object credentials;
  @Getter private Object environment;

  private CustomUsernamePasswordAuthenticationToken(
      Object environment, Object principal, Object credentials) {
    super(null);
    this.principal = principal;
    this.credentials = credentials;
    this.environment = environment;
    this.setAuthenticated(false);
  }

  private CustomUsernamePasswordAuthenticationToken(
      Object environment,
      Object principal,
      Object credentials,
      Collection<? extends GrantedAuthority> authorities) {
    super(authorities);
    this.principal = principal;
    this.credentials = credentials;
    this.environment = environment;
    super.setAuthenticated(true);
  }

  public static CustomUsernamePasswordAuthenticationToken unauthenticated(
      Object environment, Object principal, Object credentials) {
    return new CustomUsernamePasswordAuthenticationToken(environment, principal, credentials);
  }

  public static CustomUsernamePasswordAuthenticationToken authenticated(
      Object environment,
      Object principal,
      Object credentials,
      Collection<? extends GrantedAuthority> authorities) {
    return new CustomUsernamePasswordAuthenticationToken(
        environment, principal, credentials, authorities);
  }

  public Object getCredentials() {
    return this.credentials;
  }

  public Object getPrincipal() {
    return this.principal;
  }

  public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
    Assert.isTrue(
        !isAuthenticated,
        "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
    super.setAuthenticated(false);
  }

  public void eraseCredentials() {
    super.eraseCredentials();
    this.credentials = null;
  }
}
