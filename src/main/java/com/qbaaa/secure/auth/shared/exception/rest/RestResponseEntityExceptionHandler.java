package com.qbaaa.secure.auth.shared.exception.rest;

import com.qbaaa.secure.auth.shared.exception.DomainExistsException;
import com.qbaaa.secure.auth.shared.exception.EmailAlreadyExistsException;
import com.qbaaa.secure.auth.shared.exception.InputInvalidException;
import com.qbaaa.secure.auth.shared.exception.LoginException;
import com.qbaaa.secure.auth.shared.exception.RegisterException;
import com.qbaaa.secure.auth.shared.exception.RestErrorCodeType;
import com.qbaaa.secure.auth.shared.exception.UnSupportedFileException;
import com.qbaaa.secure.auth.shared.exception.UserNoActiveAccount;
import com.qbaaa.secure.auth.shared.exception.UsernameAlreadyExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@ControllerAdvice
@Slf4j
public class RestResponseEntityExceptionHandler {

  private static final String SERVER_ERROR_LOG = "SERVER ERROR, uuid: {}, uri: {}";

  @ExceptionHandler(EntityNotFoundException.class)
  ResponseEntity<ErrorDetails> handleEntityNotFound(
      EntityNotFoundException ex, HttpServletRequest request) {
    var errorCodeType = RestErrorCodeType.NOT_FOUND;
    var errorDetails = buildErrorDetails(errorCodeType.getErrorType(), ex.getMessage());
    log.error(SERVER_ERROR_LOG, errorDetails.uuid(), request.getRequestURI(), ex);
    return ResponseEntity.status(errorCodeType.getHttpStatus()).body(errorDetails);
  }

  @ExceptionHandler(RegisterException.class)
  ResponseEntity<ErrorDetails> handleRegisterException(
      RegisterException ex, HttpServletRequest request) {
    var errorCodeType = RestErrorCodeType.REGISTER_DISABLED_DOMAIN;
    var errorDetails = buildErrorDetails(errorCodeType.getErrorType(), ex.getMessage());
    log.error(SERVER_ERROR_LOG, errorDetails.uuid(), request.getRequestURI(), ex);
    return ResponseEntity.status(errorCodeType.getHttpStatus()).body(errorDetails);
  }

  @ExceptionHandler(BadCredentialsException.class)
  ResponseEntity<ErrorDetails> handleBadCredentialsException(
      BadCredentialsException ex, HttpServletRequest request) {
    var errorCodeType = RestErrorCodeType.CREDENTIALS_INVALIDATION;
    var errorDetails = buildErrorDetails(errorCodeType.getErrorType(), ex.getMessage());
    log.error(SERVER_ERROR_LOG, errorDetails.uuid(), request.getRequestURI(), ex);
    return ResponseEntity.status(errorCodeType.getHttpStatus()).body(errorDetails);
  }

  @ExceptionHandler(InputInvalidException.class)
  ResponseEntity<ErrorDetails> handleInputInvalidException(
      InputInvalidException ex, HttpServletRequest request) {
    var errorCodeType = RestErrorCodeType.INPUT_VALIDATION;
    var errorDetails = buildErrorDetails(errorCodeType.getErrorType(), ex.getMessage());
    log.error(SERVER_ERROR_LOG, errorDetails.uuid(), request.getRequestURI(), ex);
    return ResponseEntity.status(errorCodeType.getHttpStatus()).body(errorDetails);
  }

  @ExceptionHandler(UnSupportedFileException.class)
  ResponseEntity<ErrorDetails> handleUnSupportedFileException(
      UnSupportedFileException ex, HttpServletRequest request) {
    var errorCodeType = RestErrorCodeType.UN_SUPPORTED_FILE;
    var errorDetails = buildErrorDetails(errorCodeType.getErrorType(), ex.getMessage());
    log.error(SERVER_ERROR_LOG, errorDetails.uuid(), request.getRequestURI(), ex);
    return ResponseEntity.status(errorCodeType.getHttpStatus()).body(errorDetails);
  }

  @ExceptionHandler(LoginException.class)
  ResponseEntity<ErrorDetails> handleLoginException(LoginException ex, HttpServletRequest request) {
    var errorCodeType = RestErrorCodeType.CREDENTIALS_INVALIDATION;
    var errorDetails = buildErrorDetails(errorCodeType.getErrorType(), ex.getMessage());
    log.error(SERVER_ERROR_LOG, errorDetails.uuid(), request.getRequestURI(), ex);
    return ResponseEntity.status(errorCodeType.getHttpStatus()).body(errorDetails);
  }

  @ExceptionHandler(UserNoActiveAccount.class)
  ResponseEntity<ErrorDetails> handleUserNoActiveAccount(
      UserNoActiveAccount ex, HttpServletRequest request) {
    var errorCodeType = RestErrorCodeType.USER_NO_ACTIVE_ACCOUNT;
    var errorDetails = buildErrorDetails(errorCodeType.getErrorType(), ex.getMessage());
    log.error(SERVER_ERROR_LOG, errorDetails.uuid(), request.getRequestURI(), ex);
    return ResponseEntity.status(errorCodeType.getHttpStatus()).body(errorDetails);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  ResponseEntity<ErrorDetails> handleHandlerConstraintViolationException(
      ConstraintViolationException ex, HttpServletRequest request) {
    var errorCodeType = RestErrorCodeType.INPUT_VALIDATION;
    var errors = new HashMap<String, String>();
    ex.getConstraintViolations()
        .forEach(error -> errors.put(String.valueOf(error.getPropertyPath()), error.getMessage()));

    var errorDetails = buildErrorDetails(errorCodeType.getErrorType(), errors.toString());
    log.error(SERVER_ERROR_LOG, errorDetails.uuid(), request.getRequestURI(), ex);
    return ResponseEntity.status(errorCodeType.getHttpStatus()).body(errorDetails);
  }

  @ExceptionHandler(HandlerMethodValidationException.class)
  ResponseEntity<ErrorDetails> handleHandlerMethodValidationException(
      HandlerMethodValidationException ex, HttpServletRequest request) {
    var errorCodeType = RestErrorCodeType.INPUT_VALIDATION;
    var message = "Validation " + ex.getMessage();
    var errorDetails = buildErrorDetails(errorCodeType.getErrorType(), message);
    log.error(SERVER_ERROR_LOG, errorDetails.uuid(), request.getRequestURI(), ex);
    return ResponseEntity.status(errorCodeType.getHttpStatus()).body(errorDetails);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ErrorDetails> handleHandlerMethodValidationException(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
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

  @ExceptionHandler(UsernameAlreadyExistsException.class)
  ResponseEntity<ErrorDetails> handleUsernameExistsException(
      UsernameAlreadyExistsException ex, HttpServletRequest request) {
    var errorCodeType = RestErrorCodeType.USERNAME_ALREADY_EXISTS;
    var errorDetails = buildErrorDetails(errorCodeType.getErrorType(), ex.getMessage());
    log.error(SERVER_ERROR_LOG, errorDetails.uuid(), request.getRequestURI(), ex);
    return ResponseEntity.status(errorCodeType.getHttpStatus()).body(errorDetails);
  }

  @ExceptionHandler(EmailAlreadyExistsException.class)
  ResponseEntity<ErrorDetails> handleEmailExistsException(
      EmailAlreadyExistsException ex, HttpServletRequest request) {
    var errorCodeType = RestErrorCodeType.EMAIL_ALREADY_EXISTS;
    var errorDetails = buildErrorDetails(errorCodeType.getErrorType(), ex.getMessage());
    log.error(SERVER_ERROR_LOG, errorDetails.uuid(), request.getRequestURI(), ex);
    return ResponseEntity.status(errorCodeType.getHttpStatus()).body(errorDetails);
  }

  @ExceptionHandler(DomainExistsException.class)
  ResponseEntity<ErrorDetails> handleDomainExistsException(
      DomainExistsException ex, HttpServletRequest request) {
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
