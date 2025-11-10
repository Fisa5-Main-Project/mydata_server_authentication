package com.knowwhohow.controller;

import com.knowwhohow.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Controller
@Slf4j
@RequestMapping("/oauth")
@RequiredArgsConstructor
public class FinancialAuthController {

    // CI로 사용자를 조회하기 위한 커스텀 서비스
    // SecurityConfig에 Bean으로 등록된 것을 주입받는다.
    private final MemberService userDetailsService;

    // Spring Security가 원래 요청을 저장해둔 캐시
    private final RequestCache requestCache = new HttpSessionRequestCache();

    // SecurityContext를 세션에 명시적으로 저장하기 위한 리포지토리
    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    @GetMapping("/my-cert-auth")
    public String showCertAuthPage() {

        return "my-cert-auth-page";
    }

    @PostMapping("/my-cert-callback")
    public void handleCertCallback(@RequestParam("ci") String ci,
                                   HttpServletRequest request,
                                   HttpServletResponse response) throws IOException {

        log.info("userDetailsService 실행");
        // 전달받은 CI로
        UserDetails userDetails = userDetailsService.findOrCreateUserByCi(ci);

        log.info("Authentication 저장");
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, // principal (사용자 정보)
                null, // credentials (자격증명, 인증서로 대체했으므로 null)
                userDetails.getAuthorities() // authorities (권한 목록)
        );

        // 수동 로그인
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        securityContextRepository.saveContext(context, request, response);


        // 원래 목적지로 리디렉션
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        String targetUrl = (savedRequest != null) ? savedRequest.getRedirectUrl() : "/";

        response.sendRedirect(targetUrl);
    }
}
