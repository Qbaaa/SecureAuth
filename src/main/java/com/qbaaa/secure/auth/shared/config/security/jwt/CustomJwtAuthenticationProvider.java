package com.qbaaa.secure.auth.shared.config.security.jwt;

import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;

@RequiredArgsConstructor
public class CustomJwtAuthenticationProvider implements AuthenticationProvider {

  private final JwtService jwtService;
  private final Converter<DecodedJWT, Collection<GrantedAuthority>> jwtAuthenticationConverter;

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    if (authentication instanceof CustomJwtAuthenticationToken jwtAuthentication) {
      var token = jwtAuthentication.getCredentials();
      var issuer = jwtAuthentication.getIssuer();

      var authenticated =
          jwtService
              .verify(issuer, (String) token)
              .orElseThrow(() -> new BadCredentialsException("Invalid token"));

      var roles = jwtAuthenticationConverter.convert(authenticated);
      return CustomJwtAuthenticationToken.authenticated(authenticated, issuer, roles);
    }
    return null;
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return CustomJwtAuthenticationToken.class.isAssignableFrom(authentication);
  }
}
