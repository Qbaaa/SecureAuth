package com.qbaaa.secure.auth.controller;

import com.qbaaa.secure.auth.dto.AuthRequest;
import com.qbaaa.secure.auth.dto.LoginRequest;
import com.qbaaa.secure.auth.dto.TokenResponse;
import com.qbaaa.secure.auth.service.strategy.AuthStrategy;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth/domains/{domainName}")
@RequiredArgsConstructor
public class AuthController {

    private final Map<String, AuthStrategy> authStrategies;

    @PostMapping("token")
    public ResponseEntity<TokenResponse> token(@PathVariable String domainName, @RequestBody AuthRequest authRequest,
                                               HttpServletRequest request) {

        var type = authRequest.getType();
        var baseUrl = getBaseUrl(request);
        var token = authStrategies.get(type.getValue())
                .authenticate(domainName, baseUrl, authRequest);
       return ResponseEntity.ok(token);

    }

//    @PostMapping("/register")
//    public ResponseEntity<Void> register(@PathVariable String domainName, @RequestBody RegisterRequest registerRequest) {
//
//        return ResponseEntity.ok().build();
//    }

//    @PostMapping("logout")
//    public ResponseEntity<Void> logout(@PathVariable String domainName, @RequestBody LoginRequest request) {
//
//        return ResponseEntity.ok().build();
//    }

    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String host = request.getServerName();
        int port = request.getServerPort();

        return scheme + "://" + host + (port == 80 || port == 443 ? "" : ":" + port);
    }

}
