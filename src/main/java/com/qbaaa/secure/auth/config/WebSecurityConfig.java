package com.qbaaa.secure.auth.config;

import com.qbaaa.secure.auth.config.security.jwt.CustomJwtAuthenticationFilter;
import com.qbaaa.secure.auth.config.security.jwt.CustomJwtAuthenticationProvider;
import com.qbaaa.secure.auth.config.security.jwt.CustomJwtConverter;
import com.qbaaa.secure.auth.config.security.jwt.JwtService;
import com.qbaaa.secure.auth.config.security.jwt.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
public class WebSecurityConfig {

    private static final AntPathRequestMatcher[] WHITE_LIST = {
            antMatcher("/swagger-ui/**"),
            antMatcher("/api-secure-auth-backend/**"),
            antMatcher("/domains/*/auth/token"),
            antMatcher("/domains/*/auth/register")
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfiguration corsConfiguration() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        return configuration;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, CustomJwtAuthenticationFilter customJwtAuthenticationFilter) throws Exception {
        return http
                .authorizeHttpRequests(
                        authorize ->
                                authorize
                                        .requestMatchers(WHITE_LIST).permitAll()
                                        .requestMatchers("/admin/domains")
                                        .hasRole("master__" + Role.ADMIN.name())
                                        .requestMatchers("/domains/*/auth/logout").authenticated()
                                        .anyRequest().authenticated()
                )
                .sessionManagement(
                        sessionManagement ->
                                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)  // default enable
                .cors(config -> config.configurationSource(request -> corsConfiguration()))
                //.cors(AbstractHttpConfigurer::disable)
                .addFilterBefore(customJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class )
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(JwtService jwtService) {
        var jwtAuthenticationProvider = new CustomJwtAuthenticationProvider(jwtService, new CustomJwtConverter());

        return new ProviderManager(List.of(jwtAuthenticationProvider));
    }

}
