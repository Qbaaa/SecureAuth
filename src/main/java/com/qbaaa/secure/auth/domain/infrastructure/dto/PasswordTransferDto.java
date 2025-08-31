package com.qbaaa.secure.auth.domain.infrastructure.dto;

import jakarta.validation.constraints.NotEmpty;

public record PasswordTransferDto(@NotEmpty String password) {}
