package com.knowwhohow.controller;

import com.knowwhohow.dto.SmsCertificationConfirmDTO;
import com.knowwhohow.dto.SmsCertificationRequestDTO;
import com.knowwhohow.dto.TestSmsResponseDTO;
import com.knowwhohow.global.dto.ApiResponse;
import com.knowwhohow.service.SmsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/sms")
@RequiredArgsConstructor
public class SmsController {

    private final SmsService smsService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<?>> sendSms(@Valid @RequestBody SmsCertificationRequestDTO requestDto) {
        log.debug("Received SMS send request: {}", requestDto);
        String verificationId = smsService.certificateSMS(requestDto);
        log.debug("SMS sent, verificationId: {}", verificationId);
        return ResponseEntity.ok(ApiResponse.onSuccess(verificationId));
    }

    @PostMapping("/send-test")
    public ResponseEntity<ApiResponse<TestSmsResponseDTO>> sendTestSms(@Valid @RequestBody SmsCertificationRequestDTO requestDto) {
        log.debug("Received SMS test send request: {}", requestDto);
        TestSmsResponseDTO response = smsService.sendTestSmsCertification(requestDto);
        log.debug("SMS test sent, response: {}", response);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<String>> confirmSms(@Valid @RequestBody SmsCertificationConfirmDTO confirmDto) {
        log.debug("Received SMS confirm request: {}", confirmDto);
        String message = smsService.confirmSmsCertification(confirmDto);
        log.debug("SMS confirmed, message: {}", message);
        return ResponseEntity.ok(ApiResponse.onSuccess(message));
    }
}
