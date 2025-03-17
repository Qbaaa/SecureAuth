package com.qbaaa.secure.auth.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("secureauth.main")
@Getter
@Setter
public class SecureAuthProperties {

    private String user;
    private String email;
    private String password;

    @PostConstruct
    public void checkConfig() {
        if (user.isEmpty()) {
            user = null;
        }

        if (email.isEmpty()) {
            email = null;
        }

        if (password.isEmpty()) {
            password = null;
        }

    }

}
