package com.knowwhohow.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 400 Bad Request
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_001", "입력 값이 올바르지 않습니다."),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_001", "서버 내부 오류가 발생했습니다."),

    // spring security exception
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "SECURITY_001", "접근권한이 없습니다."),
    NOT_LOGIN_USER(HttpStatus.FORBIDDEN, "SECURITY_002", "로그인하지 않은 사용자입니다."),

    // certification exception
    NOT_USER(HttpStatus.NOT_FOUND, "CERTIFICATION_001", "사용자를 찾을 수 없습니다."),

    // sms exception
    SMS_SEND_FAILURE(HttpStatus.INTERNAL_SERVER_ERROR, "SMS_001", "SMS 전송에 실패했습니다."),
    CERTIFICATION_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "SMS_002", "인증 코드가 만료되었습니다."),
    CERTIFICATION_CODE_NOT_FOUND(HttpStatus.BAD_REQUEST, "SMS_003", "인증 코드를 찾을 수 없습니다."),
    INVALID_CERTIFICATION_CODE(HttpStatus.BAD_REQUEST, "SMS_004", "유효하지 않은 인증 코드입니다."),

    // Aes exception
    FAIL_ENCRYPT(HttpStatus.FORBIDDEN, "AES_001", "암호화 실패"),
    FAIL_DECRYPT(HttpStatus.FORBIDDEN, "AES_001", "복호화 실패");


    private final HttpStatus status;    // HTTP 상태
    private final String code;          // API 응답에 사용할 커스텀 에러 코드 (HTTP 상태 코드와 동일하게)
    private final String message;       // API 응답에 사용할 에러 메시지
}