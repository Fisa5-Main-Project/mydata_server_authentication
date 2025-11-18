package com.knowwhohow.global.exception;


import com.knowwhohow.global.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * @Valid 어노테이션을 사용한 DTO의 유효성 검증 실패 시 발생하는 예외를 처리합니다.
     *
     * @param e MethodArgumentNotValidException
     * @return 필드별 에러 메시지를 담은 400 Bad Request 응답
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        // 첫 번째 필드 에러의 메시지를 가져옵니다.
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        log.warn("Validation failed: {}", errorMessage);

        return new ResponseEntity<>(
                ApiResponse.onFailure(ErrorCode.INVALID_INPUT_VALUE.getCode(), errorMessage),
                ErrorCode.INVALID_INPUT_VALUE.getStatus()
        );
    }

    /**
     * 서비스 로직에서 발생하는 모든 CustomException을 처리합니다.
     *
     * @param e CustomException
     * @return ApiResponse.onFailure()를 사용한 일관된 에러 응답
     */
    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e) {
        // ErrorCode에서 정의한 code와 message를 가져옵니다.
        ErrorCode errorCode = e.getErrorCode();
        String code = errorCode.getCode();
        String message = errorCode.getMessage();
        HttpStatus status = errorCode.getStatus();

        // 로그 기록
        // (실제 배포 시: e.getStackTrace()를 포함하여 더 자세한 로그 필요)
        log.error("Handling CustomException: {} - {}", code, message, e);

        // ApiResponse.onFailure()를 사용하여 실패 응답을 생성합니다.
        // HTTP Status도 ErrorCode에서 정의한 것을 사용합니다.
        return new ResponseEntity<>(
                ApiResponse.onFailure(code, message),
                status
        );
    }

    /**
     * 위에서 처리하지 못한 모든 예외를 처리합니다. (Catch-all)
     *
     * @param e Exception
     * @return 500 Internal Server Error 응답
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        // 예외 스택 트레이스를 로그로 남깁니다.
        log.error("Unhandled exception caught!", e);

        // ErrorCode에 정의된 INTERNAL_SERVER_ERROR를 사용합니다.
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

        // ApiResponse.onFailure()를 사용하여 실패 응답을 생성합니다.
        return new ResponseEntity<>(
                ApiResponse.onFailure(errorCode.getCode(), errorCode.getMessage()),
                errorCode.getStatus()
        );
    }

}
