package com.qbaaa.secure.auth.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RestErrorCodeType {
  NOT_FOUND("NOT_FOUND", 404, HttpStatus.NOT_FOUND),
  EMAIL_TOKEN_EXPIRED("EMAIL_TOKEN_EXPIRED", 410, HttpStatus.GONE),
  UN_SUPPORTED_FILE("UN_SUPPORTED_FILE", 415, HttpStatus.UNSUPPORTED_MEDIA_TYPE),
  INPUT_VALIDATION("INPUT_VALIDATION", 400, HttpStatus.BAD_REQUEST),
  CREDENTIALS_INVALIDATION("CREDENTIALS_INVALIDATION", 401, HttpStatus.UNAUTHORIZED),
  DOMAIN_ALREADY_EXISTS("DOMAIN_ALREADY_EXISTS", 409, HttpStatus.CONFLICT),
  USERNAME_ALREADY_EXISTS("USERNAME_ALREADY_EXISTS", 409, HttpStatus.CONFLICT),
  EMAIL_ALREADY_EXISTS("EMAIL_ALREADY_EXISTS", 409, HttpStatus.CONFLICT),
  REGISTER_DISABLED_DOMAIN("REGISTRATION_DISABLED_DOMAIN", 403, HttpStatus.FORBIDDEN),
  FORBIDDEN("FORBIDDEN", 403, HttpStatus.FORBIDDEN),
  INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", 500, HttpStatus.INTERNAL_SERVER_ERROR);

  private final String errorType;
  private final int code;
  private final HttpStatus httpStatus;
}
