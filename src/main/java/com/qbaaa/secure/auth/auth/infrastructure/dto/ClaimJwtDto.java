package com.qbaaa.secure.auth.auth.infrastructure.dto;

import java.util.List;

public record ClaimJwtDto(
    String domainName,
    String username,
    String email,
    List<String> roles,
    String baseUrl,
    String session,
    Integer accessTokenValidity) {}
