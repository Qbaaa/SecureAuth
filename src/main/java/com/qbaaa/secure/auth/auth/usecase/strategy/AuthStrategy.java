package com.qbaaa.secure.auth.auth.usecase.strategy;

import com.qbaaa.secure.auth.auth.api.dto.AuthRequest;
import com.qbaaa.secure.auth.auth.api.dto.AuthResponse;
import org.springframework.stereotype.Component;

@Component
public abstract class AuthStrategy {

  public abstract AuthResponse authenticate(String domainName, String baseUrl, AuthRequest request);
}
