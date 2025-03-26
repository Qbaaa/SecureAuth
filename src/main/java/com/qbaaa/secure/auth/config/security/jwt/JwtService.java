package com.qbaaa.secure.auth.config.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.qbaaa.secure.auth.dto.ClaimJwtDto;
import com.qbaaa.secure.auth.service.KeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final KeyService keyService;

    private static final String ROLES_CLAIM = "roles";
    private static final String USERNAME_CLAIM = "username";
    private static final String EMAIL_CLAIM = "email";
    private static final String SESSION_CLAIM = "session";

    public String createAccessToken(ClaimJwtDto claimJwt) {
        var privateKey = keyService.getPrivateKey(claimJwt.domainName());
        var algorithm = Algorithm.RSA256(null, privateKey);

        return JWT.create()
                .withIssuer(claimJwt.baseUrl() + "/auth/domains/" + claimJwt.domainName())
                .withClaim(USERNAME_CLAIM, claimJwt.username())
                .withClaim(EMAIL_CLAIM, claimJwt.email())
                .withClaim(SESSION_CLAIM, claimJwt.session())
                .withClaim(ROLES_CLAIM, claimJwt.roles())
                .withExpiresAt(Instant.now().plusSeconds(claimJwt.accessTokenValidity()))
                .sign(algorithm);
    }

    public String createRefreshToken(String domainName, String session, Integer refreshTokenValidity) {
        var privateKey = keyService.getPrivateKey(domainName);
        var algorithm = Algorithm.RSA256(null, privateKey);

        return JWT.create()
                .withClaim(SESSION_CLAIM, session)
                .withExpiresAt(Instant.now().plusSeconds(refreshTokenValidity))
                .sign(algorithm);
    }
}
