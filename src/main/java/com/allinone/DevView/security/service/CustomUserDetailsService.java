package com.allinone.DevView.security.service;

import com.allinone.DevView.user.entity.User;
import com.allinone.DevView.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * Spring Security 사용자 인증 서비스
 * 일반 로그인(폼 로그인) 시 사용자 정보를 로드
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 이메일로 사용자를 찾아 Spring Security UserDetails로 변환
     * @param email 사용자 이메일 (username으로 사용)
     * @return UserDetails Spring Security 인증 객체
     * @throws UsernameNotFoundException 사용자를 찾을 수 없을 때
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Spring Security 사용자 조회: email={}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("사용자를 찾을 수 없음: email={}", email);
                    return new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email);
                });

        // OAuth2 사용자는 일반 로그인 불가
        if (user.getPassword() == null) {
            log.warn("OAuth2 사용자가 일반 로그인 시도: email={}", email);
            throw new UsernameNotFoundException("소셜 로그인을 이용해주세요.");
        }

        log.debug("사용자 인증 정보 생성 완료: userId={}, email={}", user.getUserId(), user.getEmail());

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                new ArrayList<>()  // 권한 목록 (현재는 사용하지 않음)
        );
    }
}