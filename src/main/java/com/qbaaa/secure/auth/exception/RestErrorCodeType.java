package com.qbaaa.secure.auth.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RestErrorCodeType {

    UN_SUPPORTED_FILE("UN_SUPPORTED_FILE",415, HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    INPUT_VALIDATION("INPUT_VALIDATION", 400, HttpStatus.BAD_REQUEST),
    CREDENTIALS_INVALIDATION("CREDENTIALS_INVALIDATION", 401, HttpStatus.UNAUTHORIZED),
    DOMAIN_ALREADY_EXISTS("DOMAIN_ALREADY_EXISTS", 422, HttpStatus.UNPROCESSABLE_ENTITY),
    FORBIDDEN("FORBIDDEN", 403, HttpStatus.FORBIDDEN),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", 500, HttpStatus.INTERNAL_SERVER_ERROR);

    private final String errorType;
    private final int code;
    private final HttpStatus httpStatus;

}
