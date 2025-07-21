package com.joycrew.backend.controller;

import com.joycrew.backend.dto.UserProfileResponse;
import com.joycrew.backend.entity.Employee;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "사용자", description = "사용자 정보 관련 API")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    @Operation(summary = "사용자 프로필 조회", description = "JWT 토큰으로 인증된 사용자의 프로필 정보를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
            @ApiResponse(responseCode = "401", description = "토큰 없음 또는 유효하지 않음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"message\": \"유효하지 않은 토큰입니다.\"}")))
    })
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "유효하지 않은 토큰입니다."));
        }

        Employee employee = (Employee) authentication.getPrincipal();

        UserProfileResponse response = new UserProfileResponse(
                employee.getEmployeeId(),
                employee.getEmployeeName(),
                employee.getEmail()
        );

        return ResponseEntity.ok(response);
    }
}
