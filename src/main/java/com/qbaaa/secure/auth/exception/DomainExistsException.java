package com.qbaaa.secure.auth.exception;

public class DomainExistsException extends RuntimeException {

  public DomainExistsException(final String domainName) {
    super(domainName);
  }
}
