package com.qbaaa.secure.auth.multifactorauth.api.dto;

import com.qbaaa.secure.auth.multifactorauth.domain.enums.OperationPublicType;

public record OperationRequest(OperationPublicType operation, String username) {}
