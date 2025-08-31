package com.qbaaa.secure.auth.shared.exception;

public class UserAlreadyExistsException extends RuntimeException {
  public UserAlreadyExistsException(String username, String email, String domainName) {
    super(username + " or " + email + " already exists in domain " + domainName);
  }
}
