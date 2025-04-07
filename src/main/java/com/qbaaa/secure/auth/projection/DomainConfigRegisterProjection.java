package com.qbaaa.secure.auth.projection;

public interface DomainConfigRegisterProjection {

    Boolean getIsEnabledRegister();
    Boolean getIsEnabledVerifiedEmail();
    Integer getEmailTokenValidity();

}
