package com.qbaaa.secure.auth.service.strategy;

import com.qbaaa.secure.auth.dto.AuthRequest;
import com.qbaaa.secure.auth.dto.TokenResponse;
import org.springframework.stereotype.Component;

@Component
public abstract class AuthStrategy {

  public abstract TokenResponse authenticate(
      String domainName, String baseUrl, AuthRequest request);
}
