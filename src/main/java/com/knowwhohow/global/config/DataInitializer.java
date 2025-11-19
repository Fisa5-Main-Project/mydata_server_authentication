package com.knowwhohow.global.config;


import com.knowwhohow.global.entity.CertificationUser;
import com.knowwhohow.global.entity.Member;
import com.knowwhohow.repository.CertificationUserRepository;
import com.knowwhohow.repository.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
public class DataInitializer implements ApplicationRunner {

    private final Logger log = LoggerFactory.getLogger(getClass());

    // [!!] SAS의 DB 연동 리포지토리
    private final RegisteredClientRepository registeredClientRepository;
    private final PasswordEncoder passwordEncoder;

    // (Mock 유저용)
    private final CertificationUserRepository certificationUserRepository;
    private final MemberRepository memberRepository;

    public DataInitializer(
            RegisteredClientRepository registeredClientRepository,
            PasswordEncoder passwordEncoder,
            CertificationUserRepository certificationUserRepository,
            MemberRepository memberRepository) {
        this.registeredClientRepository = registeredClientRepository;
        this.passwordEncoder = passwordEncoder;
        this.certificationUserRepository = certificationUserRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        // --- [!! 바로 이 부분이 "DB에 넣는" 로직입니다 !!] ---

        // 1. DB의 'oauth2_registered_client' 테이블에 "my-client-id"가 이미 있는지 확인
        if (this.registeredClientRepository.findByClientId("my-client-id") == null) {

            // 2. DB에 없다면, Java 코드로 'my-client-id' 정보 생성
            RegisteredClient oidcClient = RegisteredClient.withId(UUID.randomUUID().toString())
                    .clientId("my-client-id")
                    // [핵심 보안] "my-client-secret" 원본이 아닌, "암호화(해시)"된 값을
                    .clientSecret(passwordEncoder.encode("my-client-secret"))
                    .clientName("MyData Client")
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                    .redirectUri("http://127.0.0.1:8080/login/oauth2/code/my-client-id")
                    .scope(OidcScopes.OPENID)
                    .scope("my.data.read")
                    .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
                    .build();

            // 3. [!! INSERT !!]
            // 'oauth2_registered_client' 테이블에 이 정보를 "INSERT" (save) 합니다.
            this.registeredClientRepository.save(oidcClient);
            log.info(">>> [DataInitializer] Test Client 'my-client-id' registered in DB.");
        }

        // --- (이하 Mock 유저 2명 INSERT 로직) ---

        if (certificationUserRepository.findByNameAndPhoneNumber("홍길동", "01012345678").isEmpty()) {
            CertificationUser hong = new CertificationUser("홍길동", "M", LocalDate.of(1990, 1, 1), "KT", "01012345678", "ci-hong-12345");
            certificationUserRepository.save(hong);
            log.info(">>> [DataInitializer] Mock User '홍길동' created in DB.");
        }

        if (certificationUserRepository.findByNameAndPhoneNumber("김영희", "01098765432").isEmpty()) {
            CertificationUser kim = new CertificationUser("김영희", "F", LocalDate.of(1995, 2, 2), "SKT",  "01098765432", "ci-kim-67890");
            certificationUserRepository.save(kim);

            // "김영희"는 '기존 Member' 시나리오 테스트를 위해 Member 테이블에도 미리 저장
            Member kimMember = new Member("ci-kim-67890", "ROLE_USER");
            memberRepository.save(kimMember);
            log.info(">>> [DataInitializer] Mock User '김영희' and existing Member created in DB.");
        }
    }
}