package com.joycrew.backend.kyc;

import com.joycrew.backend.auth.AccountRecoveryService;
import com.joycrew.backend.kyc.crypto.NiceCrypto;
import com.joycrew.backend.kyc.dto.FindIdResult;
import com.joycrew.backend.kyc.dto.NiceStartResponse;
import com.joycrew.backend.kyc.store.InMemoryKycStore;
import com.joycrew.backend.util.MaskingUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class KycPassService {

    private final NicePassClient client;
    private final NiceCrypto crypto;
    private final NicePassProperties props;
    private final AccountRecoveryService accountRecoveryService;
    private final InMemoryKycStore store;

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter BIRTH_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public KycPassService(
            NicePassClient client,
            NiceCrypto crypto,
            NicePassProperties props,
            AccountRecoveryService accountRecoveryService
    ) {
        this.client = client;
        this.crypto = crypto;
        this.props = props;
        this.accountRecoveryService = accountRecoveryService;
        this.store = new InMemoryKycStore(props.getReqTtlSeconds());
    }

    public NiceStartResponse startFindId() {
        String reqDtim = LocalDateTime.now().format(TS);
        String reqNo   = genReqNo();

        String accessToken = client.getAccessToken();
        Map<String,Object> cryptoToken = client.getCryptoToken(accessToken);

        String tokenVal = (String) cryptoToken.getOrDefault("tokenVal", cryptoToken.get("token_value"));
        String tokenVersionId = (String) cryptoToken.getOrDefault("tokenVersionId", props.getTokenVersionId());

        Map<String,Object> body = Map.of(
                "dataHeader", Map.of("CNTY_CD", "ko"),
                "dataBody", Map.of(
                        "req_dtim", reqDtim,
                        "req_no", reqNo,
                        "enc_mode", "1",
                        "return_url", props.getReturnUrl(),
                        "product_id", props.getProductId()
                )
        );

        var pack = crypto.encryptRequest(tokenVal, reqDtim, reqNo, body);
        store.put(reqNo, tokenVal);

        return NiceStartResponse.builder()
                .tokenVersionId(tokenVersionId)
                .encData(pack.encData())               // ✅ record 기본 접근자
                .integrityValue(pack.integrityValue()) // ✅ record 기본 접근자
                .requestNo(reqNo)
                .standardWindowUrl(props.getStandardWindowUrl())
                .build();
    }

    public FindIdResult handleCallback(String tokenVersionId, String encData, String integrityValue, String reqNo, String reqDtim) {
        String tokenVal = store.takeIfFresh(reqNo);
        if (tokenVal == null) throw new IllegalStateException("요청번호가 만료되었거나 유효하지 않습니다.");

        Map<String,Object> result = crypto.decryptCallback(tokenVal, reqDtim, reqNo, encData);

        String name     = str(result, "name");
        String mobileNo = str(result, "mobile_no");
        String birthStr = str(result, "birthday");
        LocalDate birth = parseBirth(birthStr);

        Optional<String> loginId = accountRecoveryService.findLoginIdByIdentity(name, birth, mobileNo);

        return FindIdResult.builder()
                .matched(loginId.isPresent())
                .maskedLoginId(MaskingUtil.maskEmail(loginId.orElse(null)))
                .build();
    }

    private static String str(Map<String,Object> m, String k) {
        Object v = m.get(k);
        return v == null ? null : String.valueOf(v);
    }

    private static LocalDate parseBirth(String yyyymmdd) {
        if (yyyymmdd == null || yyyymmdd.length() != 8) return null;
        return LocalDate.parse(yyyymmdd, BIRTH_FMT);
    }

    private String genReqNo() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 30);
    }
}
