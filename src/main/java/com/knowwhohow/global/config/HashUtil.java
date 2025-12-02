package com.knowwhohow.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Component
public class HashUtil {

    private final String secretKey;
    private static final String ALGORITHM = "HmacSHA256";

    public HashUtil(@Value("${encrypt.hash-key}") String secretKey) {
        this.secretKey = secretKey;
    }

    /**
     * 입력받은 문자열을 HMAC-SHA256으로 해싱하여 Hex String으로 반환
     */
    public String generateHash(String message) {
        if (message == null) return null;

        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            mac.init(secretKeySpec);

            byte[] hashBytes = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }

    // 바이트 배열을 16진수 문자열로 변환 (DB 저장용)
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}