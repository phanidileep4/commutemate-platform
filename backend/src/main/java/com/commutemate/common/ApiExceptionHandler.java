package com.commutemate.common;

import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler(IllegalArgumentException.class)
  ResponseEntity<?> badRequest(IllegalArgumentException ex) {
    return ResponseEntity.badRequest().body(Map.of("timestamp", Instant.now(), "error", ex.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<?> validation(MethodArgumentNotValidException ex) {
    var msg = ex.getBindingResult().getFieldErrors().stream()
        .findFirst().map(e -> e.getField() + ": " + e.getDefaultMessage()).orElse("Validation failed");
    return ResponseEntity.badRequest().body(Map.of("timestamp", Instant.now(), "error", msg));
  }

  @ExceptionHandler(SecurityException.class)
  ResponseEntity<?> forbidden(SecurityException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("timestamp", Instant.now(), "error", ex.getMessage()));
  }
}
