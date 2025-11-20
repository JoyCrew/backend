package com.joycrew.backend.controller;

import com.joycrew.backend.dto.*;
import com.joycrew.backend.service.AuthService;
import com.joycrew.backend.web.CookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
  public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request,
                                             HttpServletRequest httpReq) {
    LoginResponse body = authService.login(request);

    // 운영/개발에 맞게 설정
    boolean secure = true; // prod는 true 고정 권장
    long maxAgeSec = 24 * 60 * 60; // access 토큰 유효시간과 동일하게
    String cookieDomain = ".joycrew.co.kr";

    // 쿠키에 JWT 심기
    var cookie = CookieUtil.authCookie(body.accessToken(), cookieDomain, maxAgeSec, secure);

    return ResponseEntity.ok()
            .header("Set-Cookie", cookie.toString())
            .body(body);
  }

  @Operation(summary = "Logout")
  @PostMapping("/logout")
  public ResponseEntity<SuccessResponse> logout(HttpServletRequest request) {
    authService.logout(request);

    boolean secure = true;
    String cookieDomain = ".joycrew.co.kr";

    var clear = CookieUtil.clearAuth(cookieDomain, secure);
    return ResponseEntity.ok()
            .header("Set-Cookie", clear.toString())
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
