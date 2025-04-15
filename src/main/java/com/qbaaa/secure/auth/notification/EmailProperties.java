package com.qbaaa.secure.auth.notification;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("secureauth.mail")
@Getter
@Setter
public class EmailProperties {

  private String notificationEmail;
  private String activeAccountTemplate;
}
