package com.joycrew.backend.controller;

import com.joycrew.backend.dto.*;
import com.joycrew.backend.security.UserPrincipal;
import com.joycrew.backend.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "User", description = "APIs related to user information")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

  private final EmployeeService employeeService;

  @Operation(summary = "Get user profile", security = @SecurityRequirement(name = "Authorization"))
  @GetMapping("/profile")
  public ResponseEntity<UserProfileResponse> getProfile(
      @AuthenticationPrincipal UserPrincipal principal
  ) {
    return ResponseEntity.ok(employeeService.getUserProfile(principal.getUsername()));
  }

  @Operation(summary = "Change password", security = @SecurityRequirement(name = "Authorization"))
  @PostMapping("/password")
  public ResponseEntity<SuccessResponse> forceChangePassword(
      @AuthenticationPrincipal UserPrincipal principal,
      @Valid @RequestBody PasswordChangeRequest request
  ) {
    employeeService.forcePasswordChange(principal.getUsername(), request);
    return ResponseEntity.ok(new SuccessResponse("Password changed successfully."));
  }

  @Operation(summary = "Update my information", description = "Send profile data as 'request' part and image as 'profileImage' part in a multipart/form-data request.", security = @SecurityRequirement(name = "Authorization"))
  @PatchMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<SuccessResponse> updateMyProfile(
      @AuthenticationPrincipal UserPrincipal principal,
      @RequestPart("request") UserProfileUpdateRequest request,
      @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {

    employeeService.updateUserProfile(principal.getUsername(), request, profileImage);
    return ResponseEntity.ok(new SuccessResponse("Your information has been updated successfully."));
  }

  @Operation(summary = "Verify current password", security = @SecurityRequirement(name = "Authorization"))
  @PostMapping("/password/verify")
  public ResponseEntity<SuccessResponse> verifyPassword(
          @AuthenticationPrincipal UserPrincipal principal,
          @Valid @RequestBody PasswordVerifyRequest request
  ) {
    employeeService.verifyCurrentPassword(principal.getUsername(), request);
    return ResponseEntity.ok(new SuccessResponse("Password verified successfully."));
  }
}
