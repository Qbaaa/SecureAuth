package com.qbaaa.secure.auth.auth.usecase.strategy;

import com.qbaaa.secure.auth.auth.api.dto.AuthRequest;
import com.qbaaa.secure.auth.auth.api.dto.TokenResponse;
import org.springframework.stereotype.Component;

@Component
public abstract class AuthStrategy {

  public abstract TokenResponse authenticate(
      String domainName, String baseUrl, AuthRequest request);
}
