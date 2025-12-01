package com.knowwhohow.global.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Converter
@Component
@RequiredArgsConstructor
public class EncryptConverter implements AttributeConverter<String, String> {

    private final AesUtil aesUtil;

    // DB에 저장할 때 (Insert/Update) -> 암호화 실행
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        return aesUtil.encrypt(attribute);
    }

    // DB에서 불러올 때 (Select) -> 복호화 실행
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return aesUtil.decrypt(dbData);
    }
}