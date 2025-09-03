package com.qbaaa.secure.auth.shared.exception;

public class AccountLockedException extends RuntimeException {
  public AccountLockedException(final String domainName, final String username) {
    super("Account locked for user " + username + " for domain " + domainName);
  }
}
