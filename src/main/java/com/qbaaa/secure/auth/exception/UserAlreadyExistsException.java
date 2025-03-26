package com.qbaaa.secure.auth.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String username, String email) {
      super(username + "or " + email + " already exists");
    }
}
