package com.qbaaa.secure.auth.auth.domian.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("secureauth.lock.account.login")
@Getter
@Setter
public class AccountLockedProperties {

  private Integer attempt;
  private Integer time;
}
