package com.knowwhohow.controller;

import com.knowwhohow.dto.FetchCertRequestDTO;
import com.knowwhohow.dto.FetchCertResponseDTO;
import com.knowwhohow.global.dto.ApiResponse;
import com.knowwhohow.service.CertAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/cert")
@RequiredArgsConstructor
public class CertAuthController {

    private final CertAuthService certAuthService;

    @PostMapping("/fetch")
    public ResponseEntity<ApiResponse<?>> fetchCertificate(
            @RequestBody FetchCertRequestDTO request) {
        log.info("회원 찾기");
        // 1. SMS 인증번호가 유효한지 먼저 검증

        // 2. 서비스 로직 호출
        FetchCertResponseDTO response = certAuthService.fetchAndSaveCertUser(request);
        log.info("서비스 로직 호출");
        // 3. 성공 응답
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }
}
