package com.knowwhohow.global.config;

import com.knowwhohow.global.entity.Member;
import com.knowwhohow.repository.MemberRepository;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Optional;
import java.util.UUID;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final MemberRepository memberRepository;

    /**
     * 인가 서버(SAS) 보안 필터 체인
     * - @Order(1)으로 가장 높은 우선순위
     */
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http)
            throws Exception {

        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                OAuth2AuthorizationServerConfigurer.authorizationServer();

        http
                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                .with(authorizationServerConfigurer, (authorizationServer) ->
                        authorizationServer
                                .authorizationEndpoint(authorizationEndpoint ->
                                        authorizationEndpoint.consentPage("/oauth2/consent")
                                )
                                .oidc(Customizer.withDefaults())
                )
                .authorizeHttpRequests((authorize) ->
                        authorize
                                .anyRequest().authenticated()
                )
                .exceptionHandling((exceptions) -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/oauth/my-cert-auth"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                );
        return http.build();
    }


    /**
     * 일반 사용자 인증 및 웹 보안 필터 체인
     * - @Order(2)로 SAS 다음 순서의 우선순위
     */
    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http)
            throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // '/my-cert-auth'와 '/my-cert-callback'은 모두에게 허용
                        .requestMatchers("/oauth/my-cert-auth",
                                "/oauth/my-cert-callback",
                                "/api/v1/cert/**",
                                "/css/**",
                                "/js/**",
                                "/error",
                                "/favicon.ico"
                        ).permitAll()
                        // 그 외 모든 요청은 반드시 '인증'을 요구
                        .anyRequest().authenticated()
                )
                // ID/PW 기반의 폼 로그인을 사용하지 않도록 비활성화
                .formLogin(AbstractHttpConfigurer::disable)
                // HTTP Basic 인증도 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)
                // 금융 인증서 콜백은 외부에서 POST로 호출될 수 있으므로 CSRF를 비활성화
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    // --- SAS 및 Security에 반드시 필요한 Bean 설정들 --- //


    /**
     * [필수] 비밀번호 인코더 (PasswordEncoder)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * JWT 서명 키 소스 (JWKSource)
     * - Access Token을 JWT로 발급할 때 사용할 RSA 키 페어(공개키/개인키)를 제공
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcRegisteredClientRepository(jdbcTemplate);
    }

    // RSA 키 페어를 생성하는 헬퍼 메서드
    private static KeyPair generateRsaKey() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * JWT 인코더 (JwtEncoder)
     * - JWKSource의 키를 사용하여 JWT를 실제로 인코딩
     */
    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

     /**
     * 인가 서버 세부 설정 (AuthorizationServerSettings)
     * - Issuer URI (발급자 주소) 등을 설정
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        // http://localhost:9000 (인가 서버의 주소)
        return AuthorizationServerSettings.builder().build();
    }

    /**
     * JWT 토큰 커스터마이저(OAuth2TokenCustomizer)
     * - Access Token에 추가적인 정보(claims)를 답기 위해 사용
     */
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
        return (context) -> {
            if(context.getTokenType().getValue().equals("access_token")) {
                String ci = context.getPrincipal().getName();
                Optional<Member> memberOptional = memberRepository.findByCi(ci);

                if (memberOptional.isPresent()) {
                    Member member = memberOptional.get();
                    context.getClaims().claim("ci", member.getCi());
                }
            }
        };
    }
}
