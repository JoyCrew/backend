package com.joycrew.backend.controller;

import com.joycrew.backend.dto.RecognitionRequest;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.service.RecognitionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "인정/보상", description = "동료 간 포인트 보상 API")
@RestController
@RequestMapping("/api/recognitions")
@RequiredArgsConstructor
public class RecognitionController {
    private final RecognitionService recognitionService;

    @PostMapping
    public ResponseEntity<Map<String, String>> sendPoints(
            @AuthenticationPrincipal Employee sender,
            @Valid @RequestBody RecognitionRequest request
    ) {
        recognitionService.sendRecognition(sender.getEmail(), request);
        return ResponseEntity.ok(Map.of("message", "포인트를 성공적으로 보냈습니다."));
    }
}