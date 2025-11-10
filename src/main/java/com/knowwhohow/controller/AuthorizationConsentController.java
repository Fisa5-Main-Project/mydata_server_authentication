package com.knowwhohow.controller;

import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.Set;

@Controller
@RequestMapping("/oauth2")
public class AuthorizationConsentController {

    private final RegisteredClientRepository registeredClientRepository;

    public AuthorizationConsentController(RegisteredClientRepository registeredClientRepository) {
        this.registeredClientRepository = registeredClientRepository;
    }

    @GetMapping("/consent")
    public String consent(Principal principal, Model model,
                          @RequestParam(OAuth2ParameterNames.CLIENT_ID) String clientId,
                          @RequestParam(OAuth2ParameterNames.SCOPE) String scope,
                          @RequestParam(OAuth2ParameterNames.STATE) String state) {

        // 클라이언트 정보 조회
        RegisteredClient client = this.registeredClientRepository.findByClientId(clientId);
        String clientName = (client != null) ? client.getClientName() : clientId;

        // 요청된 Scope들을 Set으로 변환
        Set<String> scopes = StringUtils.commaDelimitedListToSet(
                StringUtils.arrayToDelimitedString(
                        StringUtils.delimitedListToStringArray(scope, " "), ","
                )
        );

        // Model에 템플릿이 사용할 데이터 추가
        model.addAttribute("clientId", clientId);
        model.addAttribute("clientName", clientName);
        model.addAttribute("state", state);
        model.addAttribute("scopes", scopes);
        model.addAttribute("username", principal.getName());

        return "oauth2-consent-page";
    }
}
