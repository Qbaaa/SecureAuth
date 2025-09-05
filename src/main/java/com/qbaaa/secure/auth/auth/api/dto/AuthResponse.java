package com.qbaaa.secure.auth.auth.api.dto;

public sealed interface AuthResponse permits TokenResponse, MfaResponse {}
