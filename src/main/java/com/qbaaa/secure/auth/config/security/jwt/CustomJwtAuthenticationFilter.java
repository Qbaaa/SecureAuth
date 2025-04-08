package com.qbaaa.secure.auth.config.security.jwt;

import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

import com.qbaaa.secure.auth.config.SecureAuthProperties;
import com.qbaaa.secure.auth.util.UrlUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class CustomJwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String TOKEN_PREFIX = "Bearer ";
  private static final String PREFIX_ISSUER = "/domains/";

  private final SecureAuthProperties secureAuthProperties;
  private final AuthenticationConfiguration authenticationConfiguration;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    var authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (StringUtils.hasText(authHeader) && authHeader.startsWith(TOKEN_PREFIX)) {
      var token = authHeader.substring(TOKEN_PREFIX.length());
      var baseUrl = UrlUtil.getBaseUrl(request);
      var issuer = baseUrl + PREFIX_ISSUER + secureAuthProperties.getDomain();
      if (request.getRequestURL().toString().startsWith(baseUrl + PREFIX_ISSUER)) {
        issuer = UrlUtil.extractDomain(request);
      }

      var customJwtAuthentication = CustomJwtAuthenticationToken.unauthenticated(token, issuer);
      try {
        var authentication =
            authenticationConfiguration
                .getAuthenticationManager()
                .authenticate(customJwtAuthentication);
        save(authentication);
      } catch (AuthenticationException authenticationException) {
        response.setStatus(SC_UNAUTHORIZED);
      } catch (Exception exception) {
        throw new ServletException(exception);
      }
    }
    filterChain.doFilter(request, response);
  }

  private void save(Authentication authentication) {
    var context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(authentication);
    SecurityContextHolder.setContext(context);
  }
}
