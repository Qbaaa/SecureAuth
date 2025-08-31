package com.qbaaa.secure.auth.shared.event;

import lombok.Value;

@Value
public class AccountActiveEvent {

  String baseUrl;
  String domainName;
  String username;
  String email;
  String token;
}
