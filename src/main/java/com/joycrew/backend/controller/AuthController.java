package com.joycrew.backend.controller;

import com.joycrew.backend.dto.*;
import com.joycrew.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "APIs for login and password reset")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Login")
    @ApiResponse(responseCode = "200", description = "Login successful")
    @ApiResponse(responseCode = "401", description = "Authentication failed")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        // 1. 서비스에서 인증 처리 및 토큰 발급
        LoginResponse loginResponse = authService.login(request);

        // 2. 프론트엔드 요구사항에 맞춰 쿠키 생성
        ResponseCookie cookie = ResponseCookie.from("accessToken", loginResponse.accessToken())
                .path("/")
                .sameSite("None")
                .secure(true)
                .httpOnly(false)
                .domain(".joycrew.co.kr")
                .maxAge(60 * 60)
                .build();

        // 3. 헤더에 쿠키를 포함하여 응답 반환
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(loginResponse);
    }

    @Operation(summary = "Logout")
    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse> logout(HttpServletRequest request) {
        authService.logout(request);

        // [추가] 로그아웃 시 클라이언트의 쿠키도 삭제해주는 것이 정석입니다.
        ResponseCookie deleteCookie = ResponseCookie.from("accessToken", "")
                .path("/")
                .sameSite("None")
                .secure(true)
                .httpOnly(false)
                .domain(".joycrew.co.kr")
                .maxAge(0) // 시간을 0으로 설정하여 즉시 삭제
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .body(new SuccessResponse("You have been logged out."));
    }

    @Operation(summary = "Request password reset (sends email)", description = "Sends a magic link to the user's email to reset the password.")
    @ApiResponse(responseCode = "200", description = "The request was processed successfully (the response is the same regardless of whether the email exists).")
    @PostMapping("/password-reset/request")
    public ResponseEntity<SuccessResponse> requestPasswordReset(@RequestBody @Valid PasswordResetRequest request) {
        authService.requestPasswordReset(request.email());
        return ResponseEntity.ok(new SuccessResponse("A password reset email has been requested. Please check your email."));
    }

    @Operation(summary = "Confirm password reset", description = "Finalizes the password change using the token from the email and the new password.")
    @ApiResponse(responseCode = "200", description = "Password changed successfully.")
    @ApiResponse(responseCode = "400", description = "The token is invalid or has expired.")
    @PostMapping("/password-reset/confirm")
    public ResponseEntity<SuccessResponse> confirmPasswordReset(@RequestBody @Valid PasswordResetConfirmRequest request) {
        authService.confirmPasswordReset(request.token(), request.newPassword());
        return ResponseEntity.ok(new SuccessResponse("Password changed successfully."));
    }
}