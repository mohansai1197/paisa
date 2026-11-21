package com.paisa.api;

import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiErrors {
  @ExceptionHandler(DisabledException.class)
  ResponseEntity<?> unverified() {
    return ResponseEntity.status(403)
        .body(
            Map.of(
                "message",
                "Verify your email before signing in. Request a verification code to continue.",
                "verificationRequired",
                true));
  }

  @ExceptionHandler(ResponseStatusException.class)
  ResponseEntity<?> status(ResponseStatusException e) {
    return ResponseEntity.status(e.getStatusCode())
        .body(Map.of("message", e.getReason() == null ? "Request failed." : e.getReason()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<?> invalid(MethodArgumentNotValidException e) {
    return ResponseEntity.badRequest()
        .body(
            Map.of(
                "message",
                "Check the form fields and try again.",
                "fields",
                e.getBindingResult().getFieldErrors().stream()
                    .map(f -> f.getField() + ": " + f.getDefaultMessage())
                    .toList()));
  }

  @ExceptionHandler(AuthenticationException.class)
  ResponseEntity<?> authentication() {
    return ResponseEntity.status(401).body(Map.of("message", "Email or password is incorrect."));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<?> duplicate() {
    return ResponseEntity.status(409)
        .body(Map.of("message", "This record already exists or is used by another record."));
  }

  @ExceptionHandler({HttpMessageNotReadableException.class, IllegalArgumentException.class})
  ResponseEntity<?> malformed() {
    return ResponseEntity.badRequest()
        .body(Map.of("message", "Check the values and date format, then try again."));
  }
}
