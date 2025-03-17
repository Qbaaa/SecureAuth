package com.qbaaa.secure.auth.dto;

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
        List<UserTransferDto> users,
        List<RoleTransferDto> roles
) {
}
