package com.qbaaa.secure.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Username is required")
        @Size(min = 4, max = 50)
        @Pattern(regexp = "^[A-Za-z0-9_.-]+$",
                message = "Username contains forbidden characters")
        String username,
        @NotBlank(message = "Email is required")
        @Email
        @Pattern(regexp = "^[A-Za-z0-9@_.-]+$",
                message = "Email contains forbidden characters")
        String email,
        @NotBlank(message = "Password is required")
        @Size(min = 6)
        String password
) {
}
