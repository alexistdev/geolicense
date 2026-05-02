/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.exceptions;

import com.alexistdev.geolicense.dto.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ResponseData<Void>> handleNotFound(NotFoundException ex) {
        log.error("Not found: {}", ex.getMessage());
        ResponseData<Void> response = new ResponseData<>();
        response.setStatus(false);
        response.getMessages().add(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(ExistingException.class)
    public ResponseEntity<ResponseData<Void>> handleConflict(ExistingException ex) {
        log.error("Conflict: {}", ex.getMessage());
        ResponseData<Void> response = new ResponseData<>();
        response.setStatus(false);
        response.getMessages().add(ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseData<Void>> handleValidation(MethodArgumentNotValidException ex) {
        ResponseData<Void> response = new ResponseData<>();
        response.setStatus(false);
        ex.getBindingResult().getAllErrors()
                .forEach(err -> response.getMessages().add(err.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseData<Void>> handleGeneral(Exception ex) {
        log.error("Unexpected error", ex);
        ResponseData<Void> response = new ResponseData<>();
        response.setStatus(false);
        response.getMessages().add("Error: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(org.springframework.security.authentication.LockedException.class)
    public ResponseEntity<ResponseData<Void>> handleLocked(org.springframework.security.authentication.LockedException ex) {
        log.warn("Attempt to login with locked account: {}", ex.getMessage());
        ResponseData<Void> response = new ResponseData<>();
        response.setStatus(false);
        response.getMessages().add("Your account has been suspended.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ResponseEntity<ResponseData<Void>> handleBadCredentials(org.springframework.security.authentication.BadCredentialsException ex) {
        log.warn("Bad credentials login attempt: {}", ex.getMessage());
        ResponseData<Void> response = new ResponseData<>();
        response.setStatus(false);
        response.getMessages().add("Invalid email or password.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(LicenseExpiredException.class)
    public ResponseEntity<ResponseData<Void>> handleExpired(LicenseExpiredException ex) {
        log.warn("Your license has expired: {}", ex.getMessage());
        ResponseData<Void> response = new ResponseData<>();
        response.setStatus(false);
        response.getMessages().add("License is expired.");
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(response);
    }

    @ExceptionHandler(LicenseForbiddenException.class)
    public ResponseEntity<ResponseData<Void>> handleForbidden(LicenseForbiddenException ex) {
        log.warn("Unauthorized Licensed", ex);
        ResponseData<Void> response = new ResponseData<>();
        response.setStatus(false);
        response.getMessages().add("Unauthorized Licensed.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(SeatLimitReachedException.class)
    public ResponseEntity<ResponseData<Void>> handleSeatLimit(SeatLimitReachedException ex) {
        log.warn("License has reach limit", ex);
        ResponseData<Void> response = new ResponseData<>();
        response.setStatus(false);
        response.getMessages().add("License has reach limit.");
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }

}
