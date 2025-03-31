package com.qbaaa.secure.auth.config.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.qbaaa.secure.auth.dto.ClaimJwtDto;
import com.qbaaa.secure.auth.service.KeyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final KeyService keyService;

    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_SESSION = "session";
    private static final String CLAIM_DOMAIN = "domain";

    public String createAccessToken(ClaimJwtDto claimJwt) {
        var privateKey = keyService.getPrivateKey(claimJwt.domainName());
        var algorithm = Algorithm.RSA256(null, privateKey);

        return JWT.create()
                .withIssuer(claimJwt.baseUrl() + "/auth/domains/" + claimJwt.domainName())
                .withClaim(CLAIM_USERNAME, claimJwt.username())
                .withClaim(CLAIM_EMAIL, claimJwt.email())
                .withClaim(CLAIM_SESSION, claimJwt.session())
                .withClaim(CLAIM_ROLES, claimJwt.roles())
                .withClaim(CLAIM_DOMAIN, claimJwt.domainName())
                .withExpiresAt(Instant.now().plusSeconds(claimJwt.accessTokenValidity()))
                .sign(algorithm);
    }

    public String createRefreshToken(String domainName, String session, Integer refreshTokenValidity) {
        var privateKey = keyService.getPrivateKey(domainName);
        var algorithm = Algorithm.RSA256(null, privateKey);

        return JWT.create()
                .withClaim(CLAIM_SESSION, session)
                .withExpiresAt(Instant.now().plusSeconds(refreshTokenValidity))
                .sign(algorithm);
    }

    public Optional<DecodedJWT> verify(String issuer, String token) {
        try {
            var domainName = issuer.substring(issuer.lastIndexOf('/') + 1);
            var publicKey = keyService.getPublicKey(domainName);
            var algorithm = Algorithm.RSA256(publicKey, null);
            var verifier = JWT
                    .require(algorithm)
                    .withIssuer(issuer)
                    .build();

            var jwt = verifier.verify(token);
            return Optional.of(jwt);
        } catch (JWTVerificationException jwtVerificationException) {
            log.error(jwtVerificationException.getMessage());
            return Optional.empty();
        }
    }
}
