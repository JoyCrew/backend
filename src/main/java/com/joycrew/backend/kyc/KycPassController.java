package com.joycrew.backend.kyc;

import com.joycrew.backend.kyc.dto.FindIdResult;
import com.joycrew.backend.kyc.dto.NiceStartResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kyc/pass")
@RequiredArgsConstructor
public class KycPassController {

    private final KycPassService service;

    // 1) 프론트: 아이디찾기 시작 → 표준창 파라미터 수급
    @PostMapping("/start/find-id")
    public NiceStartResponse startFindId() {
        return service.startFindId();
    }

    // 2) PASS 콜백 (NICE가 호출)
    @RequestMapping("/callback")
    public FindIdResult callback(
            @RequestParam("token_version_id") String tokenVersionId,
            @RequestParam("enc_data") String encData,
            @RequestParam("integrity_value") String integrityValue,
            @RequestParam(value = "req_dtim", required = false) String reqDtim,
            @RequestParam(value = "req_no", required = false) String reqNo
    ) {
        if (reqNo == null || reqDtim == null) {
            throw new IllegalStateException("요청정보 누락(req_no/req_dtim)");
        }
        return service.handleCallback(tokenVersionId, encData, integrityValue, reqNo, reqDtim);
    }
}
