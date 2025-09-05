package com.qbaaa.secure.auth.multifactorauth.api.controller;

import com.qbaaa.secure.auth.auth.api.dto.TokenResponse;
import com.qbaaa.secure.auth.multifactorauth.api.dto.MfaRequest;
import com.qbaaa.secure.auth.multifactorauth.usecase.CreateAuthTokenUseCase;
import com.qbaaa.secure.auth.shared.util.UrlUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/domains/{domainName}/auth")
@Tag(name = "Multi-factor authentication API")
@RequiredArgsConstructor
public class MfaController {

  private final CreateAuthTokenUseCase createAuthTokenUseCase;

  @Operation(security = @SecurityRequirement(name = "Authorization"))
  @PostMapping("login/mfa")
  public ResponseEntity<TokenResponse> loginMfa(
      @PathVariable String domainName,
      @RequestBody MfaRequest mfaRequest,
      HttpServletRequest request) {
    var baseUrl = UrlUtil.getBaseUrl(request);

    return ResponseEntity.ok(
        createAuthTokenUseCase.createAuthToken(mfaRequest, baseUrl, domainName));
  }
}
