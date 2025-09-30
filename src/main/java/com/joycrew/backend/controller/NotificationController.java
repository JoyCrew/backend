package com.joycrew.backend.controller;

import com.joycrew.backend.dto.NotificationResponse;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.repository.EmployeeRepository;
import com.joycrew.backend.security.UserPrincipal;
import com.joycrew.backend.service.NotificationService;
import com.joycrew.backend.service.NotificationSseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationSseService sseService;
    private final EmployeeRepository employeeRepository;

    @Operation(summary = "미확인 알림 가져오기", security = @SecurityRequirement(name = "Authorization"))
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getUnreadForToast(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "false") boolean all,
            @RequestParam(defaultValue = "72") int sinceHours
    ) {
        List<NotificationResponse> list = all
                ? notificationService.getRecentSince(principal.getUsername(), sinceHours)
                : notificationService.getUnreadForToast(principal.getUsername());
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "알림 읽음 처리", security = @SecurityRequirement(name = "Authorization"))
    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markRead(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        notificationService.markRead(id, principal.getUsername());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "SSE 구독 (실시간 알림 스트림)", security = @SecurityRequirement(name = "Authorization"))
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        String email = principal.getUsername();

        Employee me = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        if (Boolean.FALSE.equals(me.getAppNotificationEnabled())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Notifications disabled");
        }
        return sseService.subscribe(email);
    }
}
