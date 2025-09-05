package com.qbaaa.secure.auth.auth.api.controller;

import com.qbaaa.secure.auth.auth.api.dto.AuthRequest;
import com.qbaaa.secure.auth.auth.api.dto.RefreshTokenRequest;
import com.qbaaa.secure.auth.auth.usecase.LogoutUseCase;
import com.qbaaa.secure.auth.auth.usecase.strategy.AuthStrategy;
import com.qbaaa.secure.auth.shared.util.UrlUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/domains/{domainName}/auth")
@RequiredArgsConstructor
@Tag(name = "Auth API")
public class AuthController {

  private final Map<String, AuthStrategy> authStrategies;
  private final LogoutUseCase logoutUseCase;

  @PostMapping("token")
  public ResponseEntity<?> token(
      @PathVariable String domainName,
      @RequestBody AuthRequest authRequest,
      HttpServletRequest request) {
    var type = authRequest.getType();
    var baseUrl = UrlUtil.getBaseUrl(request);
    var token = authStrategies.get(type.getValue()).authenticate(domainName, baseUrl, authRequest);
    return ResponseEntity.ok(token);
  }

  @PostMapping("logout")
  public ResponseEntity<Void> logout(
      @PathVariable String domainName,
      @RequestBody RefreshTokenRequest refreshTokenRequest,
      HttpServletRequest request) {
    var baseUrl = UrlUtil.getBaseUrl(request);
    logoutUseCase.logout(domainName, baseUrl, refreshTokenRequest);
    return ResponseEntity.noContent().build();
  }
}
