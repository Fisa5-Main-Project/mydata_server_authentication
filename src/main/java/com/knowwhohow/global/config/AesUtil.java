package com.knowwhohow.global.config;

import com.knowwhohow.global.exception.CustomException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static com.knowwhohow.global.exception.ErrorCode.FAIL_DECRYPT;
import static com.knowwhohow.global.exception.ErrorCode.FAIL_ENCRYPT;

@Component
public class AesUtil {

    @Value("${encrypt.secret-key}")
    private String key;

    private static String secretKey;
    private static final String ALGORITHM = "AES";

    @PostConstruct
    public void init() {
        secretKey = key;

        System.out.println(">>> AesUtil Init Completed. Key Length: " + (secretKey != null ? secretKey.length() : "null"));
    }

    public static String encrypt(String plainText) {
        if(plainText == null) return null;
        try {
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new CustomException(FAIL_ENCRYPT);
        }
    }

    public static String decrypt(String cipherText) {
        if(cipherText == null) return null;
        try {
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decoded = Base64.getDecoder().decode(cipherText);
            return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new CustomException(FAIL_DECRYPT);
        }
    }
}