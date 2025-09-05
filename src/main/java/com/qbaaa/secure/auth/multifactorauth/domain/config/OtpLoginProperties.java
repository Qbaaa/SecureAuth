package com.qbaaa.secure.auth.multifactorauth.domain.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("secureauth.mfa.login.mail")
@Getter
@Setter
public class OtpLoginProperties {

  private String sender;
  private String template;
}
