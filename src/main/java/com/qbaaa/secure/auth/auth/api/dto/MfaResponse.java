package com.qbaaa.secure.auth.auth.api.dto;

public record MfaResponse(String status, String pendingToken) implements AuthResponse {}
