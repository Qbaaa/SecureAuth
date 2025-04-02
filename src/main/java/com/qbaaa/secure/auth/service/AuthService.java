package com.qbaaa.secure.auth.service;

import com.qbaaa.secure.auth.config.security.jwt.JwtService;
import com.qbaaa.secure.auth.dto.RefreshTokenRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String CLAIM_SESSION = "session";

    private final JwtService jwtService;
    private final SessionServer sessionServer;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public void logout(String domainName, String baseUrl, RefreshTokenRequest refreshTokenRequest) {
        var refreshToken = refreshTokenRequest.getToken();
        if (!refreshTokenService.existsRefreshToken(refreshToken)) {
            throw new BadCredentialsException("Refresh token not found");
        }
        var issuer = baseUrl + "/domains/" + domainName;
        var authenticated = jwtService.verify(issuer, refreshToken)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        var session = authenticated.getClaim(CLAIM_SESSION).asString();
        var sessionUuid = UUID.fromString(session);

        refreshTokenService.deleteRefreshToken(refreshToken);
        sessionServer.delete(sessionUuid);
    }
}
