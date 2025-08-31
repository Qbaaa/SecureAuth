package com.qbaaa.secure.auth.shared.email;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("secureauth.mail.active-account")
@Getter
@Setter
public class ActiveAccountEmailProperties {

  private String sender;
  private String template;
  private String path;
}
