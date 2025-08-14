package com.joycrew.backend.exception;

import com.joycrew.backend.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(InsufficientPointsException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleInsufficientPoints(InsufficientPointsException ex, HttpServletRequest req) {
    return new ErrorResponse("INSUFFICIENT_POINTS", ex.getMessage(), LocalDateTime.now(), req.getRequestURI());
  }

  @ExceptionHandler(NoSuchElementException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ErrorResponse handleNoSuchElement(NoSuchElementException ex, HttpServletRequest req) {
    return new ErrorResponse("NOT_FOUND", ex.getMessage(), LocalDateTime.now(), req.getRequestURI());
  }

  @ExceptionHandler(IllegalStateException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleIllegalState(IllegalStateException ex, HttpServletRequest req) {
    return new ErrorResponse("ORDER_CANNOT_CANCEL", ex.getMessage(), LocalDateTime.now(), req.getRequestURI());
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleAuthenticationFailed(BadCredentialsException ex, HttpServletRequest req) { // 1. HttpServletRequest 추가
    ErrorResponse errorResponse = new ErrorResponse(
        "AUTHENTICATION_FAILED",
        ex.getMessage(),
        LocalDateTime.now(),
        req.getRequestURI()
    );
    return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
  }
}
