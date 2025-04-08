package com.qbaaa.secure.auth.projection;

public interface DomainConfigValidityProjection {

  Integer getAccessTokenValidity();

  Integer getRefreshTokenValidity();

  Integer getEmailTokenValidity();

  Integer getSessionValidity();
}
