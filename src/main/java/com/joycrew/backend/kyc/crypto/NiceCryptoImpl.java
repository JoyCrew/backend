package com.joycrew.backend.kyc.crypto;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class NiceCryptoImpl implements NiceCrypto {

    private final ObjectMapper objectMapper;

    @Override
    public EncPack encryptRequest(String tokenVal, String reqDtim, String reqNo, Map<String, Object> requestBodyJson) {
        try {
            Keys keys = deriveKeys(tokenVal, reqDtim, reqNo);

            byte[] plain = objectMapper.writeValueAsBytes(requestBodyJson);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keys.keySpec(), new IvParameterSpec(keys.ivBytes()));
            byte[] encrypted = cipher.doFinal(plain);

            String encData = Base64.getEncoder().encodeToString(encrypted);

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(keys.hmacKeyBytes(), "HmacSHA256"));
            byte[] h = mac.doFinal(encData.getBytes(StandardCharsets.UTF_8));
            String integrity = Base64.getEncoder().encodeToString(h);

            return new EncPack(encData, integrity);
        } catch (Exception e) {
            throw new IllegalStateException("NICE 암호화 실패", e);
        }
    }

    @Override
    public Map<String, Object> decryptCallback(String tokenVal, String reqDtim, String reqNo, String encData) {
        try {
            Keys keys = deriveKeys(tokenVal, reqDtim, reqNo);

            byte[] cipherBytes = Base64.getDecoder().decode(encData);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keys.keySpec(), new IvParameterSpec(keys.ivBytes()));
            byte[] plain = cipher.doFinal(cipherBytes);

            return objectMapper.readValue(plain, Map.class);
        } catch (Exception e) {
            throw new IllegalStateException("NICE 복호화 실패", e);
        }
    }

    // ====== 여기에 NICE 제공 샘플코드 '그대로' 붙여넣기 ======
    private Keys deriveKeys(String tokenVal, String reqDtim, String reqNo) throws Exception {
    /*
      TODO: NICE 가이드의 키 파생 규칙(AES 256 Key, IV, HMAC Key 생성)을 정확히 구현하세요.
      - 일반적으로 tokenVal + req_dtim + req_no 등을 조합해 key/iv/hmacKey를 파생
      - 상품/버전마다 세부식 상이 → 공급사 샘플 기준 구현
    */
        throw new UnsupportedOperationException("Implement key derivation as per NICE sample.");
    }
    // ====================================================

    private static byte[] sha256(byte[] in) throws Exception {
        MessageDigest d = MessageDigest.getInstance("SHA-256");
        return d.digest(in);
    }

    // ⚠ record 기본 접근자(key(), iv(), hmacKey())와 이름 겹치지 않도록 변경
    private record Keys(byte[] key, byte[] iv, byte[] hmacKey) {
        SecretKeySpec keySpec() { return new SecretKeySpec(key, "AES"); }
        byte[] ivBytes() { return iv; }
        byte[] hmacKeyBytes() { return hmacKey; }
    }
}
