package com.qbaaa.secure.auth.controller;

import com.qbaaa.secure.auth.dto.AuthRequest;
import com.qbaaa.secure.auth.dto.RefreshTokenRequest;
import com.qbaaa.secure.auth.dto.RegisterRequest;
import com.qbaaa.secure.auth.dto.TokenResponse;
import com.qbaaa.secure.auth.service.AuthService;
import com.qbaaa.secure.auth.service.strategy.AuthStrategy;
import com.qbaaa.secure.auth.util.UrlUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/domains/{domainName}/auth")
@RequiredArgsConstructor
public class AuthController {

  private final Map<String, AuthStrategy> authStrategies;
  private final AuthService authService;

  @PostMapping("token")
  public ResponseEntity<TokenResponse> token(
      @PathVariable String domainName,
      @RequestBody AuthRequest authRequest,
      HttpServletRequest request) {
    var type = authRequest.getType();
    var baseUrl = UrlUtil.getBaseUrl(request);
    var token = authStrategies.get(type.getValue()).authenticate(domainName, baseUrl, authRequest);
    return ResponseEntity.ok(token);
  }

  @PostMapping("/register")
  public ResponseEntity<String> register(
      @PathVariable String domainName,
      @RequestBody @Valid RegisterRequest registerRequest,
      HttpServletRequest request) {
    var baseUrl = UrlUtil.getBaseUrl(request);

    return ResponseEntity.ok(authService.register(baseUrl, domainName, registerRequest));
  }

  @GetMapping("/account/activate")
  public ResponseEntity<String> activeAccount(
      @PathVariable String domainName, @RequestParam String token, HttpServletRequest request) {
    var baseUrl = UrlUtil.getBaseUrl(request);

    authService.activeAccount(baseUrl, domainName, token);
    return ResponseEntity.ok("Account activated");
  }

  @PostMapping("logout")
  public ResponseEntity<Void> logout(
      @PathVariable String domainName,
      @RequestBody RefreshTokenRequest refreshTokenRequest,
      HttpServletRequest request) {
    var baseUrl = UrlUtil.getBaseUrl(request);
    authService.logout(domainName, baseUrl, refreshTokenRequest);
    return ResponseEntity.noContent().build();
  }
}
