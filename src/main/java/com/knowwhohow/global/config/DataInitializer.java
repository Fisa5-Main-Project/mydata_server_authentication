//package com.knowwhohow.global.config;
//
//
//import com.knowwhohow.global.entity.CertificationUser;
//import com.knowwhohow.global.entity.Member;
//import com.knowwhohow.repository.CertificationUserRepository;
//import com.knowwhohow.repository.MemberRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDate;
//
//@Component
//@RequiredArgsConstructor
//public class DataInitializer implements ApplicationRunner {
//
//    private final CertificationUserRepository certificationUserRepository;
//    private final MemberRepository memberRepository;
//
//    @Override
//    public void run(ApplicationArguments args) throws Exception {
//
//        // --- 테스트 사용자 1: 홍길동 (신규 Member 테스트용) ---
//        // 이 사용자는 'certification_user' 테이블에만 존재합니다.
//        // 따라서 'MemberService'가 'findOrCreate' 로직에서 'Create'를 실행하게 됩니다.
//        CertificationUser user1 = new CertificationUser(
//                "홍길동",
//                "M",
//                LocalDate.of(1990, 1, 1),
//                "KT",
//                "01012345678",
//                "ci-hong-12345" // 홍길동 CI
//        );
//        certificationUserRepository.save(user1);
//
//
//        // --- 테스트 사용자 2: 김영희 (기존 Member 테스트용) ---
//        // 이 사용자는 'certification_user'와 'member' 테이블 모두에 존재합니다.
//        // 따라서 'MemberService'가 'findOrCreate' 로직에서 'Find'를 실행하게 됩니다.
//        CertificationUser user2 = new CertificationUser(
//                "김영희",
//                "F",
//                LocalDate.of(1992, 2, 2),
//                "SKT",
//                "01098765432",
//                "ci-kim-67890" // 김영희 CI
//        );
//        certificationUserRepository.save(user2);
//
//        Member member2 = new Member(
//                "ci-kim-67890", // 김영희 CI
//                "ROLE_USER"
//        );
//        memberRepository.save(member2);
//    }
//}