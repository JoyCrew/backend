package com.joycrew.backend.controller;

import com.joycrew.backend.dto.LoginRequest;
import com.joycrew.backend.dto.LoginResponse;
import com.joycrew.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "인증", description = "로그인 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "로그인", description = "이메일과 비밀번호를 이용해 JWT 토큰과 사용자 정보를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (이메일 또는 비밀번호 오류)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"accessToken\": \"\", \"message\": \"이메일 또는 비밀번호가 올바르지 않습니다.\"}")))
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        try {
            return ResponseEntity.ok(authService.login(request));
        } catch (UsernameNotFoundException | BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(LoginResponse.builder()
                            .accessToken("")
                            .message(e.getMessage())
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(LoginResponse.builder()
                            .accessToken("")
                            .message("서버 오류가 발생했습니다.")
                            .build());
        }
    }

    @Operation(summary = "로그아웃", description = "사용자의 로그아웃 요청을 처리합니다. 클라이언트는 이 응답을 받은 후 토큰을 삭제해야 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그아웃 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"message\": \"로그아웃 되었습니다.\"}")))
    })
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(Map.of("message", "로그아웃 되었습니다."));
    }
}