package com.qbaaa.secure.auth.user.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "Username is required")
        @Size(min = 4, max = 50)
        @Pattern(regexp = "^[A-Za-z0-9_.-]+$", message = "Username contains forbidden characters")
        String username,
    @NotBlank(message = "Email is required")
        @Email
        @Pattern(regexp = "^[A-Za-z0-9@_.-]+$", message = "Email contains forbidden characters")
        String email,
    @NotBlank(message = "Password is required")
        @Size(min = 8, max = 64, message = "Password must be at least 8 characters long")
        String password,
    @NotBlank(message = "Confirm password is required")
        @Size(min = 8, max = 64, message = "Password must be at least 8 characters long")
        String confirmPassword) {}
