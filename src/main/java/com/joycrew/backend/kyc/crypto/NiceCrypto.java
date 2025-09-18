package com.joycrew.backend.kyc.crypto;

import java.util.Map;

public interface NiceCrypto {
    EncPack encryptRequest(String tokenVal, String reqDtim, String reqNo, Map<String,Object> requestBodyJson);
    Map<String,Object> decryptCallback(String tokenVal, String reqDtim, String reqNo, String encData);

    record EncPack(String encData, String integrityValue) {}
}
