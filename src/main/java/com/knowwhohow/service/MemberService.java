package com.knowwhohow.service;

import com.knowwhohow.global.config.AesUtil;
import com.knowwhohow.global.entity.Member;
import com.knowwhohow.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserDetails findOrCreateUserByCi(String ci) {

        String originCi = AesUtil.decrypt(ci);

        log.info("findOrCreateUserByCi 실행");
        Member member = memberRepository.findByCi(originCi)
                .orElseGet(() -> {
                    Member newMember = new Member(originCi, "ROLE_USER");
                    return memberRepository.save(newMember);
                });
        log.info("memberRepository 실행");
        return buildUserDetails(member);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByCi(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with CI: " + username));

        return buildUserDetails(member);
    }

    private UserDetails buildUserDetails(Member member) {
        String notUsedPassword = passwordEncoder.encode("CI_AUTH_USER_NO_PASS");

        String encryptCi = AesUtil.encrypt(member.getCi());

        return User.builder()
                .username(encryptCi)
                .password(notUsedPassword)
                .authorities(getAuthorities(member.getRoles()))
                .build();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(String roles) {
        return Arrays.stream(roles.split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
