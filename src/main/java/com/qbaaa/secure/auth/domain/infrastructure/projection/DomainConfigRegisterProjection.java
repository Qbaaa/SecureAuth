package com.qbaaa.secure.auth.domain.infrastructure.projection;

public interface DomainConfigRegisterProjection {

  Boolean getIsEnabledRegister();

  Boolean getIsEnabledVerifiedEmail();

  Integer getEmailTokenValidity();
}
