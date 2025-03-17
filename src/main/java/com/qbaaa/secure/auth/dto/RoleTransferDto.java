package com.qbaaa.secure.auth.dto;

import jakarta.validation.constraints.NotEmpty;

public record RoleTransferDto(
        @NotEmpty
        String name,
        String description
) {
}
