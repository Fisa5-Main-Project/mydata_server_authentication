package com.knowwhohow.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_001", "서버 내부 오류가 발생했습니다."),

    // spring security exception
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "SECURITY_001", "접근권한이 없습니다."),
    NOT_LOGIN_USER(HttpStatus.FORBIDDEN, "SECURITY_002", "로그인하지 않은 사용자입니다."),

    // certification exception
    NOT_USER(HttpStatus.NOT_FOUND, "CERTIFICATION_001", "사용자를 찾을 수 없습니다.");


    private final HttpStatus status;    // HTTP 상태
    private final String code;          // API 응답에 사용할 커스텀 에러 코드 (HTTP 상태 코드와 동일하게)
    private final String message;       // API 응답에 사용할 에러 메시지
}