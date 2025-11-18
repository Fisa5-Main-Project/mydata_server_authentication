package com.knowwhohow.service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.knowwhohow.dto.SmsCertificationConfirmDTO;
import com.knowwhohow.dto.SmsCertificationRequestDTO;
import com.knowwhohow.dto.TestSmsResponseDTO;
import com.knowwhohow.global.config.CoolSmsProperties;
import com.knowwhohow.global.exception.CustomException;
import com.knowwhohow.global.exception.ErrorCode;
import com.solapi.sdk.SolapiClient;
import com.solapi.sdk.message.exception.SolapiMessageNotReceivedException;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SmsService {
    private final CacheManager cacheManager;
    private final CoolSmsProperties coolSmsProperties;
    private DefaultMessageService messageService;

    @PostConstruct
    private void init() {
        log.debug("Initializing CoolSMS message service.");
        this.messageService = SolapiClient.INSTANCE.createInstance(
                coolSmsProperties.getApiKey(),
                coolSmsProperties.getApiSecret()
        );
    }


    @Transactional
    public String certificateSMS(SmsCertificationRequestDTO request) {
        log.debug("Attempting to send SMS for request: {}", request);
        String verificationId = UUID.randomUUID().toString();
        String certificationCode = createCertificationCode();
        Message message = new Message();
        message.setFrom(coolSmsProperties.getFromNumber());
        message.setTo(request.getPhoneNumber());
        message.setText("[KnowWhoHow] 본인확인 인증번호는 [" + certificationCode + "] 입니다.");

        try {
            this.messageService.send(message);
            SmsVerificationData data = new SmsVerificationData(certificationCode, request, false);
            Cache cache = Objects.requireNonNull(cacheManager.getCache("sms-verification"));
            cache.put(verificationId, data);
            log.debug("SMS verification data cached with ID: {}", verificationId);
            log.debug("SMS sent successfully to {} with code {}", request.getPhoneNumber(), certificationCode);
            return verificationId;
        } catch (SolapiMessageNotReceivedException e) {
            log.error("SMS 전송 실패, 상세 정보: {}", e.getFailedMessageList());
            log.error("에러 메시지: {}", e.getMessage());
            throw new CustomException(ErrorCode.SMS_SEND_FAILURE);
        } catch (Exception e) {
            log.error("SMS 인증 처리 중 알 수 없는 오류 발생", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 테스트용 SMS 인증 번호를 발급하고, 인증 정보를 Redis에 저장합니다.
     * 실제 SMS는 발송하지 않습니다.
     *
     * @param requestDto SMS 인증 요청 DTO
     * @return TestSmsResponseDto containing "verificationId" and "authCode"
     */
    public TestSmsResponseDTO sendTestSmsCertification(SmsCertificationRequestDTO requestDto) {
        log.debug("Attempting to send test SMS for request: {}", requestDto);
        String verificationId = UUID.randomUUID().toString();
        String certificationCode = "123456"; // /test-sms에서 쓰이는 테스트 코드(고정)

        try {
            SmsVerificationData data = new SmsVerificationData(certificationCode, requestDto, false);
            Objects.requireNonNull(cacheManager.getCache("sms-verification")).put(verificationId, data);
            log.debug("Test SMS verification data cached with ID: {}", verificationId);

            log.info("테스트용 SMS 인증 정보 생성 완료. verificationId: {}, authCode: {}", verificationId, certificationCode);
            return new TestSmsResponseDTO(verificationId, certificationCode);
        } catch (Exception e) {
            log.error("테스트용 SMS 인증 정보 생성 중 오류 발생", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * SMS 인증 번호를 확인합니다.
     *
     * @param confirmDto SMS 인증 확인 DTO
     * @return 인증 성공 메시지
     */
    public String confirmSmsCertification(SmsCertificationConfirmDTO confirmDto) {
        log.debug("Attempting to confirm SMS for verificationId: {}", confirmDto.getVerificationId());
        String verificationId = confirmDto.getVerificationId();
        SmsVerificationData data = Objects.requireNonNull(cacheManager.getCache("sms-verification")).get(verificationId, SmsVerificationData.class);

        if (data == null) {
            log.warn("Certification code not found for verificationId: {}", verificationId);
            throw new CustomException(ErrorCode.CERTIFICATION_CODE_NOT_FOUND);
        }
        log.debug("Retrieved cached data for verificationId: {}", verificationId);

        if (!data.getCertificationCode().equals(confirmDto.getAuthCode())) {
            log.warn("Invalid certification code for verificationId: {}", verificationId);
            throw new CustomException(ErrorCode.INVALID_CERTIFICATION_CODE);
        }

        SmsVerificationData updatedData = new SmsVerificationData(data.getCertificationCode(), data.getUserData(), true);
        Objects.requireNonNull(cacheManager.getCache("sms-verification")).put(verificationId, updatedData);
        log.debug("SMS verification successful and data updated for verificationId: {}", verificationId);

        return "인증번호가 일치합니다.";
    }

    public SmsCertificationRequestDTO getUserVerificationData(String verificationId) {
        log.debug("Attempting to get user verification data for verificationId: {}", verificationId);
        SmsVerificationData data = Objects.requireNonNull(cacheManager.getCache("sms-verification")).get(verificationId, SmsVerificationData.class);
        if (data == null || !data.isConfirmed()) {
            log.warn("User verification data not found or not confirmed for verificationId: {}", verificationId);
            throw new CustomException(ErrorCode.CERTIFICATION_CODE_NOT_FOUND); // 인증되지 않았거나 만료됨
        }
        log.debug("User verification data retrieved for verificationId: {}", verificationId);
        return data.getUserData();
    }

    public void removeUserVerificationData(String verificationId) {
        log.debug("Attempting to remove user verification data for verificationId: {}", verificationId);
        Objects.requireNonNull(cacheManager.getCache("sms-verification")).evict(verificationId);
        log.debug("User verification data removed for verificationId: {}", verificationId);
    }

    /**
     * 6자리 랜덤 인증 코드를 생성합니다.
     *
     * @return 생성된 인증 코드
     */
    private String createCertificationCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000); // 100000 ~ 999999
        log.debug("Generated certification code: {}", code);
        return String.valueOf(code);
    }

    @Getter
    private static class SmsVerificationData implements Serializable {
        private final String certificationCode;
        private final SmsCertificationRequestDTO userData;
        @JsonProperty("confirmed")
        private final boolean isConfirmed;

        @JsonCreator
        public SmsVerificationData(
                @JsonProperty("certificationCode") String certificationCode,
                @JsonProperty("userData") SmsCertificationRequestDTO userData,
                @JsonProperty("confirmed") boolean isConfirmed) {
            this.certificationCode = certificationCode;
            this.userData = userData;
            this.isConfirmed = isConfirmed;
        }
    }
}
