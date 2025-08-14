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
    LoginResponse loginResponse = authService.login(request);
    return ResponseEntity.ok(loginResponse);
  }

  @Operation(summary = "Logout")
  @PostMapping("/logout")
  public ResponseEntity<SuccessResponse> logout(HttpServletRequest request) {
    authService.logout(request);
    return ResponseEntity.ok(new SuccessResponse("You have been logged out."));
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
