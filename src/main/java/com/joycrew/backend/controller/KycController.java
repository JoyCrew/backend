package com.joycrew.backend.controller;

import com.joycrew.backend.dto.kyc.PhoneStartRequest;
import com.joycrew.backend.dto.kyc.PhoneStartResponse;
import com.joycrew.backend.dto.kyc.PhoneVerifyRequest;
import com.joycrew.backend.dto.kyc.PhoneVerifyResponse;
import com.joycrew.backend.service.PhoneVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/kyc/phone")
@RequiredArgsConstructor
public class KycController {
    private final PhoneVerificationService svc;

    @PostMapping("/start")
    public PhoneStartResponse start(@RequestBody @Valid PhoneStartRequest req) {
        var r = svc.start(req.phone());
        return new PhoneStartResponse(r.requestId(), r.resendAvailableInSec());
    }

    @PostMapping("/verify")
    public PhoneVerifyResponse verify(@RequestBody @Valid PhoneVerifyRequest req) {
        var r = svc.verify(req.requestId(), req.code());
        return new PhoneVerifyResponse(r.verified(), r.kycToken());
    }
}