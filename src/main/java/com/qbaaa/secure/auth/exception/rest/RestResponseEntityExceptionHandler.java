package com.qbaaa.secure.auth.exception.rest;

import com.qbaaa.secure.auth.exception.DomainExistsException;
import com.qbaaa.secure.auth.exception.LoginException;
import com.qbaaa.secure.auth.exception.RestErrorCodeType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;

@ControllerAdvice
@Slf4j
public class RestResponseEntityExceptionHandler {

    private static final String SERVER_ERROR_LOG = "SERVER ERROR, uuid: {}, uri: {}";

    @ExceptionHandler(LoginException.class)
    ResponseEntity<ErrorDetails> handleLoginException(LoginException ex, HttpServletRequest request) {
        var errorCodeType = RestErrorCodeType.LOGIN_INVALIDATION;
        var errorDetails = buildErrorDetails(errorCodeType.getErrorType(), ex.getMessage());
        log.error(SERVER_ERROR_LOG, errorDetails.uuid(), request.getRequestURI(), ex);
        return ResponseEntity.status(errorCodeType.getHttpStatus()).body(errorDetails);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ErrorDetails> handleHandlerConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {
        var errorCodeType = RestErrorCodeType.INPUT_VALIDATION;
        var errors = new HashMap<String, String>();
        ex.getConstraintViolations()
                .forEach(
                        error -> errors.put(String.valueOf(error.getPropertyPath()), error.getMessage()));

        var errorDetails = buildErrorDetails(errorCodeType.getErrorType(), errors.toString());
        log.error(SERVER_ERROR_LOG, errorDetails.uuid(), request.getRequestURI(), ex);
        return ResponseEntity.status(errorCodeType.getHttpStatus()).body(errorDetails);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    ResponseEntity<ErrorDetails> handleHandlerMethodValidationException(HandlerMethodValidationException ex, HttpServletRequest request) {
        var errorCodeType = RestErrorCodeType.INPUT_VALIDATION;
        var message = "Validation " + ex.getMessage();
        var errorDetails = buildErrorDetails(errorCodeType.getErrorType(), message);
        log.error(SERVER_ERROR_LOG, errorDetails.uuid(), request.getRequestURI(), ex);
        return ResponseEntity.status(errorCodeType.getHttpStatus()).body(errorDetails);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorDetails> handleHandlerMethodValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        var errorCodeType = RestErrorCodeType.INPUT_VALIDATION;
        var errors = new HashMap<String, String>();
        ex.getBindingResult()
                .getAllErrors()
                .forEach(
                        error -> {
                            String fieldName = ((FieldError) error).getField();
                            String errorMessage = error.getDefaultMessage();
                            errors.put(fieldName, errorMessage);
                        });

        var errorDetails = buildErrorDetails(errorCodeType.getErrorType(), errors.toString());
        log.error(SERVER_ERROR_LOG, errorDetails.uuid(), request.getRequestURI(), ex);
        return ResponseEntity.status(errorCodeType.getHttpStatus()).body(errorDetails);
    }

    @ExceptionHandler(DomainExistsException.class)
    ResponseEntity<ErrorDetails> handleDomainExistsException(DomainExistsException ex, HttpServletRequest request) {
        var errorCodeType = RestErrorCodeType.DOMAIN_ALREADY_EXISTS;
        var errorDetails = buildErrorDetails(errorCodeType.getErrorType(), ex.getMessage());
        log.error(SERVER_ERROR_LOG, errorDetails.uuid(), request.getRequestURI(), ex);
        return ResponseEntity.status(errorCodeType.getHttpStatus()).body(errorDetails);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorDetails> handleExceptionException(Exception ex, HttpServletRequest request) {
        var errorCodeType = RestErrorCodeType.INTERNAL_SERVER_ERROR;
        var errorDetails = buildErrorDetails(errorCodeType.getErrorType(), ex.getMessage());
        log.error(SERVER_ERROR_LOG, errorDetails.uuid(), request.getRequestURI(), ex);
        return ResponseEntity.status(errorCodeType.getHttpStatus()).body(errorDetails);
    }

    private ErrorDetails buildErrorDetails(String restErrorType, String message) {
        final var timeNow = LocalDateTime.now();
        final var errorId = UUID.randomUUID();
        return new ErrorDetails(timeNow, errorId, restErrorType, message);
    }

}
