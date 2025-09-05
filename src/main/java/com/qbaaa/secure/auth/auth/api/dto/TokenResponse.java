package com.qbaaa.secure.auth.auth.api.dto;

public record TokenResponse(String accessToken, String refreshToken) implements AuthResponse {}
