package com.knowwhohow.global.config;

import com.knowwhohow.global.exception.CustomException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import static com.knowwhohow.global.exception.ErrorCode.*;

@Component
public class AesUtil {

    @Value("${encrypt.secret-key}")
    private String key;

    private static SecretKey secretKeySpec;

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128; // 인증 태그 길이
    private static final int IV_LENGTH_BYTE = 12;  // GCM 권장 IV 길이 (96bit)

    @PostConstruct
    public void init() {
        if (key == null || key.length() != 32) {
            throw new CustomException(SHORT_KEY);
        }

        secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
    }

    public static String encrypt(String value) {
        if (value == null) return null;
        try {
            // 매번 랜덤한 IV(초기화 벡터) 생성
            byte[] iv = new byte[IV_LENGTH_BYTE];
            new SecureRandom().nextBytes(iv);

            // Cipher 초기화 (GCM 모드)
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, gcmSpec);

            // 암호화 수행
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));

            // [IV + 암호문] 형태로 합치기 (복호화 때 IV가 필요하므로)
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encrypted.length);
            byteBuffer.put(iv);
            byteBuffer.put(encrypted);

            // Base64 인코딩해서 반환
            return Base64.getEncoder().encodeToString(byteBuffer.array());

        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(FAIL_DECRYPT);
        }
    }

    public static String decrypt(String value) {
        if (value == null) return null;
        try {
            // Base64 디코딩
            byte[] decodedBytes = Base64.getDecoder().decode(value);

            // 앞부분에서 IV 추출
            ByteBuffer byteBuffer = ByteBuffer.wrap(decodedBytes);
            byte[] iv = new byte[IV_LENGTH_BYTE];
            byteBuffer.get(iv);

            // 남은 부분(암호문) 추출
            byte[] encrypted = new byte[byteBuffer.remaining()];
            byteBuffer.get(encrypted);

            // Cipher 초기화 (추출한 IV 사용)
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcmSpec);

            // 복호화 수행
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);

        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(FAIL_DECRYPT);
        }
    }
}