package com.qbaaa.secure.auth.dto;

import jakarta.validation.constraints.NotEmpty;

public record PasswordTransferDto(@NotEmpty String password) {}
