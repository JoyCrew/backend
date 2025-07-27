package com.joycrew.backend.controller;

import com.joycrew.backend.dto.PasswordChangeRequest;
import com.joycrew.backend.dto.SuccessResponse;
import com.joycrew.backend.dto.UserProfileResponse;
import com.joycrew.backend.security.UserPrincipal;
import com.joycrew.backend.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "사용자", description = "사용자 정보 관련 API")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final EmployeeService employeeService;

    @Operation(summary = "사용자 프로필 조회", security = @SecurityRequirement(name = "Authorization"))
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(employeeService.getUserProfile(principal.getUsername()));
    }

    @Operation(summary = "비밀번호 변경", security = @SecurityRequirement(name = "Authorization"))
    @PostMapping("/password")
    public ResponseEntity<SuccessResponse> forceChangePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody PasswordChangeRequest request
    ) {
        employeeService.forcePasswordChange(principal.getUsername(), request);
        return ResponseEntity.ok(new SuccessResponse("비밀번호가 성공적으로 변경되었습니다."));
    }
}