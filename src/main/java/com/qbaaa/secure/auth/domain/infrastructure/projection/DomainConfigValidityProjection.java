package com.qbaaa.secure.auth.domain.infrastructure.projection;

public interface DomainConfigValidityProjection {

  Integer getAccessTokenValidity();

  Integer getRefreshTokenValidity();

  Integer getEmailTokenValidity();

  Integer getSessionValidity();
}
