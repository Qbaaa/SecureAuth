package com.qbaaa.secure.auth.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UserTransferDto(
        @NotEmpty
        String username,
        @NotEmpty
        String email,
        @NotNull
        Boolean isActive,
        @NotEmpty
        Boolean isVerified,
        PasswordTransferDto password,
        List<RoleTransferDto> roles
) {
}
