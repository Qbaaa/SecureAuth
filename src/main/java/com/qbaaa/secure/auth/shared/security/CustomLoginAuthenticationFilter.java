// package com.qbaaa.secure.auth.shared.config.security;
//
// import com.fasterxml.jackson.databind.JsonNode;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import jakarta.servlet.FilterChain;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.AuthenticationException;
// import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
// import org.springframework.stereotype.Component;
//
// import java.io.IOException;
//
// @Component
// public class CustomLoginAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
//
//    public CustomLoginAuthenticationFilter(AuthenticationManager authenticationManager) {
//        super(authenticationManager);
//        setFilterProcessesUrl("/domains/*/auth/token");
//    }
//
////    @Override
////    protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse
// response) {
////        return request.getMethod().equals("POST") &&
////                request.getRequestURI().matches("/domains/[^/]+/auth/token");
////    }
//
//    @Override
//    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse
// response)
//            throws AuthenticationException {
//        var objectMapper = new ObjectMapper();
//        try {
//            JsonNode jsonNode = objectMapper.readTree(request.getInputStream());
//            String username = jsonNode.get(SPRING_SECURITY_FORM_USERNAME_KEY).asText();
//            String password = jsonNode.get(SPRING_SECURITY_FORM_PASSWORD_KEY).asText();
//            String domainName = extractDomainName(request.getRequestURI());
//
//            CustomUsernamePasswordAuthenticationToken authRequest =
//                    CustomUsernamePasswordAuthenticationToken.unauthenticated(domainName,
// username, password);
//
//            return this.getAuthenticationManager().authenticate(authRequest);
//        } catch (IOException e) {
//            throw new IllegalStateException("Failed to read JSON");
//        }
//    }
//
//    @Override
//    protected void successfulAuthentication(HttpServletRequest request,
//                                            HttpServletResponse response,
//                                            FilterChain chain,
//                                            Authentication authResult) throws IOException,
// ServletException {
//        response.setStatus(HttpServletResponse.SC_OK);
//        response.setContentType("application/json");
//
//        response.getWriter().write("""
//        {
//          "accessToken": "%s"
//        }
//        """.formatted("token"));
//    }
//
//    @Override
//    protected void unsuccessfulAuthentication(HttpServletRequest request,
//                                              HttpServletResponse response,
//                                              AuthenticationException failed) throws IOException,
// ServletException {
//        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//        response.setContentType("application/json");
//        response.getWriter().write("""
//        {
//          "error": "%s"
//        }
//        """.formatted(failed.getMessage()));
//    }
//
//    private String extractDomainName(String uri) {
//        String[] parts = uri.split("/");
//        return parts[2];
//    }
// }
