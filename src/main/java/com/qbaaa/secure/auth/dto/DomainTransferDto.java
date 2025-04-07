package com.qbaaa.secure.auth.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record DomainTransferDto(
        @NotEmpty
        String name,
        @NotNull
        @Positive
        Integer accessTokenValidity,
        @NotNull
        @Positive
        Integer refreshTokenValidity,
        @NotNull
        @Positive
        Integer emailTokenValidity,
        @NotNull
        @Positive
        Integer sessionValidity,
        Boolean isEnabledRegister,
        Boolean isEnabledVerifiedEmail,
        List<UserTransferDto> users,
        List<RoleTransferDto> roles
) {
        @JsonCreator
        public DomainTransferDto(
                @JsonProperty("name") String name,
                @JsonProperty("accessTokenValidity") Integer accessTokenValidity,
                @JsonProperty("refreshTokenValidity") Integer refreshTokenValidity,
                @JsonProperty("emailTokenValidity") Integer emailTokenValidity,
                @JsonProperty("sessionValidity") Integer sessionValidity,
                @JsonProperty("isEnabledRegister") Boolean isEnabledRegister,
                @JsonProperty("isEnabledVerifiedEmail") Boolean isEnabledVerifiedEmail,
                @JsonProperty("users") List<UserTransferDto> users,
                @JsonProperty("roles") List<RoleTransferDto> roles
        ) {
                this.name = name;
                this.accessTokenValidity = accessTokenValidity;
                this.refreshTokenValidity = refreshTokenValidity;
                this.emailTokenValidity = emailTokenValidity;
                this.sessionValidity = sessionValidity;
                this.isEnabledRegister = isEnabledRegister != null ? isEnabledRegister : Boolean.FALSE;
                this.isEnabledVerifiedEmail = isEnabledVerifiedEmail != null ? isEnabledVerifiedEmail : Boolean.FALSE;
                this.users = users;
                this.roles = roles;
        }
}
