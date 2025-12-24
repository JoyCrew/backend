package com.joycrew.backend.web;

import com.joycrew.backend.dto.ErrorResponse;
import com.joycrew.backend.exception.BillingRequiredException;
import com.joycrew.backend.exception.InsufficientPointsException;
import com.joycrew.backend.exception.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // -------------------------
    // 400 - Validation
    // -------------------------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException e,
            HttpServletRequest req
    ) {
        String msg = e.getBindingResult().getAllErrors().isEmpty()
                ? "Validation failed"
                : e.getBindingResult().getAllErrors().get(0).getDefaultMessage();

        return ResponseEntity.badRequest().body(error(
                "VALIDATION_ERROR",
                msg,
                req
        ));
    }

    // -------------------------
    // 401 - Auth
    // -------------------------
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException e,
            HttpServletRequest req
    ) {
        return ResponseEntity.status(401).body(error(
                "AUTH_FAILED",
                "Invalid email or password.",
                req
        ));
    }

    // -------------------------
    // 403 - Billing required
    // -------------------------
    @ExceptionHandler(BillingRequiredException.class)
    public ResponseEntity<ErrorResponse> handleBillingRequired(
            BillingRequiredException e,
            HttpServletRequest req
    ) {
        return ResponseEntity.status(403).body(error(
                "BILLING_REQUIRED",
                e.getMessage(),
                req
        ));
    }

    // -------------------------
    // 404 - Not Found
    // -------------------------
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(
            UserNotFoundException e,
            HttpServletRequest req
    ) {
        return ResponseEntity.status(404).body(error(
                "USER_NOT_FOUND",
                e.getMessage(),
                req
        ));
    }

    // -------------------------
    // 409 / 400 - Business rule
    // -------------------------
    @ExceptionHandler(InsufficientPointsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientPoints(
            InsufficientPointsException e,
            HttpServletRequest req
    ) {
        // 포인트 부족은 보통 409(충돌)로 주기도 하고 400으로 주기도 함.
        // 정책 확정 전이면 409 추천.
        return ResponseEntity.status(409).body(error(
                "INSUFFICIENT_POINTS",
                e.getMessage(),
                req
        ));
    }

    // -------------------------
    // 500 - Fallback
    // -------------------------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnknown(
            Exception e,
            HttpServletRequest req
    ) {
        log.error("[UNHANDLED] path={}, msg={}", req.getRequestURI(), e.getMessage(), e);

        return ResponseEntity.status(500).body(error(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred.",
                req
        ));
    }

    private ErrorResponse error(String code, String message, HttpServletRequest req) {
        return new ErrorResponse(
                code,
                message,
                LocalDateTime.now(),
                req.getRequestURI()
        );
    }
}
