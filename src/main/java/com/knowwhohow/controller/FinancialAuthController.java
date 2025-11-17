package com.knowwhohow.controller;

import com.knowwhohow.service.MemberService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Controller
@Slf4j
@RequestMapping("/oauth")
public class FinancialAuthController {

    private final MemberService userDetailsService;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
    private final SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();

    public FinancialAuthController(MemberService userDetailsService) {
        this.userDetailsService = userDetailsService;
        this.successHandler.setDefaultTargetUrl("/");
    }

    @GetMapping("/my-cert-auth")
    public String showCertAuthPage() {
        return "my-cert-auth-page";
    }

    @PostMapping("/my-cert-callback")
    public void handleCertCallback(@RequestParam("ci") String ci,
                                   HttpServletRequest request,
                                   HttpServletResponse response) throws IOException, ServletException {

        log.info("Processing my-cert-callback for CI: {}", ci);
        UserDetails userDetails = userDetailsService.findOrCreateUserByCi(ci);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        securityContextRepository.saveContext(context, request, response);

        log.info("User {} successfully authenticated. Redirecting to original request via SuccessHandler.", ci);
        successHandler.onAuthenticationSuccess(request, response, authentication);
    }
}
