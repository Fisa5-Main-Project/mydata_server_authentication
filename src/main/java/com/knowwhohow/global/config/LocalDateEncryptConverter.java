package com.knowwhohow.global.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDate;

@Converter
public class LocalDateEncryptConverter implements AttributeConverter<LocalDate, String> {

    @Override
    public String convertToDatabaseColumn(LocalDate attribute) {
        if (attribute == null) {
            return null;
        }
        // LocalDate를 String("yyyy-MM-dd")으로 변환
        String dateString = attribute.toString();

        // String을 암호화 (AesUtil 사용)
        return AesUtil.encrypt(dateString);
    }

    @Override
    public LocalDate convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        // DB에 저장된 값을 복호화
        String decrypted = AesUtil.decrypt(dbData);

        // String을 다시 LocalDate로 변환
        return LocalDate.parse(decrypted);
    }
}
