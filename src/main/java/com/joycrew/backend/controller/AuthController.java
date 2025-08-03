package com.joycrew.backend.controller;

import com.joycrew.backend.dto.*;
import com.joycrew.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증", description = "로그인 및 비밀번호 재설정 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "로그인")
    @ApiResponse(responseCode = "200", description = "로그인 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        LoginResponse loginResponse = authService.login(request);
        return ResponseEntity.ok(loginResponse);
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse> logout(HttpServletRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(new SuccessResponse("로그아웃 되었습니다."));
    }

    @Operation(summary = "비밀번호 재설정 요청 (이메일 발송)", description = "사용자 이메일로 비밀번호를 재설정할 수 있는 매직 링크를 보냅니다.")
    @ApiResponse(responseCode = "200", description = "요청이 성공적으로 처리되었습니다. (이메일 존재 여부와 상관없이 동일한 응답)")
    @PostMapping("/password-reset/request")
    public ResponseEntity<SuccessResponse> requestPasswordReset(@RequestBody @Valid PasswordResetRequest request) {
        authService.requestPasswordReset(request.email());
        return ResponseEntity.ok(new SuccessResponse("비밀번호 재설정 이메일이 요청되었습니다. 이메일을 확인해주세요."));
    }

    @Operation(summary = "비밀번호 재설정 확인", description = "이메일로 받은 토큰과 새로운 비밀번호로 비밀번호를 최종 변경합니다.")
    @ApiResponse(responseCode = "200", description = "비밀번호가 성공적으로 변경되었습니다.")
    @ApiResponse(responseCode = "400", description = "토큰이 유효하지 않거나 만료되었습니다.")
    @PostMapping("/password-reset/confirm")
    public ResponseEntity<SuccessResponse> confirmPasswordReset(@RequestBody @Valid PasswordResetConfirmRequest request) {
        authService.confirmPasswordReset(request.token(), request.newPassword());
        return ResponseEntity.ok(new SuccessResponse("비밀번호가 성공적으로 변경되었습니다."));
    }
}