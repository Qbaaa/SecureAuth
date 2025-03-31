package com.qbaaa.secure.auth.config.security.jwt;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;

public class CustomJwtConverter implements Converter<DecodedJWT, Collection<GrantedAuthority>> {

    private static final String AUTHORITY_PREFIX = "ROLE_";
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_DOMAIN = "domain";

    @Override
    public Collection<GrantedAuthority> convert(DecodedJWT jwt) {
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();

        var domain = jwt.getClaim(CLAIM_DOMAIN).asString();
        jwt.getClaim(CLAIM_ROLES).asList(String.class).forEach(
                role -> grantedAuthorities
                        .add(new SimpleGrantedAuthority(AUTHORITY_PREFIX + domain + "__" + role)));

        return grantedAuthorities;
    }

}
