package com.qbaaa.secure.auth.user.api.controller;

import com.qbaaa.secure.auth.shared.util.UrlUtil;
import com.qbaaa.secure.auth.user.api.dto.RegisterRequest;
import com.qbaaa.secure.auth.user.usecase.ActiveAccountUseCase;
import com.qbaaa.secure.auth.user.usecase.RegisterAccountUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
@RequestMapping("/domains/{domainName}/users")
@RequiredArgsConstructor
@Tag(name = "User API")
public class UserController {

  private final RegisterAccountUseCase registerAccountUseCase;
  private final ActiveAccountUseCase activeAccountUseCase;

  @PostMapping("/register")
  public ResponseEntity<String> register(
      @PathVariable String domainName,
      @RequestBody @Valid RegisterRequest registerRequest,
      HttpServletRequest request) {
    var baseUrl = UrlUtil.getBaseUrl(request);

    return ResponseEntity.ok(registerAccountUseCase.register(baseUrl, domainName, registerRequest));
  }

  @GetMapping("/account/activate")
  public ResponseEntity<String> activeAccount(
      @PathVariable String domainName, @RequestParam String token, HttpServletRequest request) {
    var baseUrl = UrlUtil.getBaseUrl(request);

    activeAccountUseCase.activeAccount(baseUrl, domainName, token);
    return ResponseEntity.ok("Account activated");
  }
}
