package com.qbaaa.secure.auth.event;

import lombok.Value;

@Value
public class AccountActiveEvent {

  String baseUrl;
  String domainName;
  String username;
  String email;
  String token;
}
