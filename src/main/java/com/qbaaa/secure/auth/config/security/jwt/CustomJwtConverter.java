package com.qbaaa.secure.auth.config.security.jwt;

import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class CustomJwtConverter implements Converter<DecodedJWT, Collection<GrantedAuthority>> {

  private static final String AUTHORITY_PREFIX = "ROLE_";
  private static final String CLAIM_ROLES = "roles";
  private static final String CLAIM_DOMAIN = "domain";

  @Override
  public Collection<GrantedAuthority> convert(DecodedJWT jwt) {
    var domain = jwt.getClaim(CLAIM_DOMAIN).asString();
    var roles = jwt.getClaim(CLAIM_ROLES).asList(String.class);

    if (domain == null || roles == null) {
      return List.of();
    }

    return roles.stream()
        .map(role -> new SimpleGrantedAuthority(AUTHORITY_PREFIX + domain + "__" + role))
        .collect(Collectors.toList());
  }
}
