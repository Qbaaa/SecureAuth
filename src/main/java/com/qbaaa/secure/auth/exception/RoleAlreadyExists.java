package com.qbaaa.secure.auth.exception;

public class RoleAlreadyExists extends RuntimeException {
    public RoleAlreadyExists(String message) {
        super(message);
    }
}
