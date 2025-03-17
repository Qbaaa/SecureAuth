package com.qbaaa.secure.auth.exception;

public class UserAlreadyException extends RuntimeException {
    public UserAlreadyException(String username, String email) {
      super(username + "or " + email + " already exists");
    }
}
