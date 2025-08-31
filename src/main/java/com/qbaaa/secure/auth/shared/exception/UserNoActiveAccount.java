package com.qbaaa.secure.auth.shared.exception;

public class UserNoActiveAccount extends RuntimeException {
  public UserNoActiveAccount(String username, String domainName) {
    super(username + " has not active account in domain " + domainName);
  }
}
