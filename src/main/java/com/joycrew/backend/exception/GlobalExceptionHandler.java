package com.joycrew.backend.exception;

import com.joycrew.backend.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException; // import 추가
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

    // '가입되지 않은 이메일' 처리
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFound(UsernameNotFoundException ex, HttpServletRequest req) {
        ErrorResponse errorResponse = new ErrorResponse(
                "AUTH_002", // 가입되지 않은 이메일
                "이메일 또는 비밀번호가 잘못되었습니다.", // 보안을 위해 메시지는 동일하게 유지
                LocalDateTime.now(),
                req.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    // '비밀번호 불일치' 처리
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationFailed(BadCredentialsException ex, HttpServletRequest req) {
        ErrorResponse errorResponse = new ErrorResponse(
                "AUTH_003", // 비밀번호 불일치
                "이메일 또는 비밀번호가 잘못되었습니다.", // 보안을 위해 메시지는 동일하게 유지
                LocalDateTime.now(),
                req.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }
}