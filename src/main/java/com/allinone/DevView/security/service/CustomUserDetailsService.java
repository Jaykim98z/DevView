package com.allinone.DevView.security.service;

import com.allinone.DevView.security.CustomUserDetails;
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
 * ✅ 개선사항: LOCAL 사용자만 처리하여 GOOGLE 사용자와 완전 분리
 * - 일반 로그인(폼 로그인) 시 LOCAL 사용자 정보만 로드
 * - GOOGLE 사용자의 잘못된 로그인 시도 차단
 * - provider별 명확한 분리로 보안 강화
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 이메일로 LOCAL 사용자를 찾아 Spring Security UserDetails로 변환
     *
     * ✅ 개선점:
     * 1. LOCAL 사용자만 조회 (GOOGLE 사용자 제외)
     * 2. 이중 체크로 보안 강화
     * 3. 명확한 에러 메시지 제공
     *
     * @param email 사용자 이메일 (username으로 사용)
     * @return UserDetails Spring Security 인증 객체
     * @throws UsernameNotFoundException 사용자를 찾을 수 없을 때
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Spring Security LOCAL 사용자 조회 시작: email={}", email);

        // 입력값 검증
        if (email == null || email.trim().isEmpty()) {
            log.warn("이메일이 비어있음");
            throw new UsernameNotFoundException("이메일은 필수입니다.");
        }

        try {
            // LOCAL 사용자만 조회 (GOOGLE 사용자는 제외)
            User user = userRepository.findLocalUserByEmail(email)
                    .orElseThrow(() -> {
                        log.warn("LOCAL 사용자를 찾을 수 없음: email={}", email);
                        return new UsernameNotFoundException("등록되지 않은 이메일이거나 소셜 로그인을 이용해주세요.");
                    });

            // 이중 체크: OAuth2 사용자가 잘못 조회된 경우 방지
            if (user.getPassword() == null || user.isGoogleUser()) {
                log.warn("OAuth2 사용자가 일반 로그인 시도: email={}, provider={}",
                        email, user.getProvider());
                throw new UsernameNotFoundException("소셜 로그인을 이용해주세요.");
            }

            // LOCAL 사용자 확인
            if (!user.isLocalUser()) {
                log.warn("LOCAL이 아닌 사용자가 로그인 시도: email={}, provider={}",
                        email, user.getProvider());
                throw new UsernameNotFoundException("일반 로그인이 불가능한 계정입니다.");
            }

            log.debug("LOCAL 사용자 인증 정보 생성 완료: userId={}, email={}, provider={}",
                    user.getUserId(), user.getEmail(), user.getProvider());

            return new CustomUserDetails(
                    user.getUserId(),
                    user.getEmail(),
                    user.getPassword(),
                    new ArrayList<>()  // 권한 목록 (현재는 사용하지 않음)
            );

        } catch (UsernameNotFoundException e) {
            // 이미 처리된 예외는 그대로 재발생
            throw e;
        } catch (Exception e) {
            log.error("사용자 조회 중 예상치 못한 오류: email={}", email, e);
            throw new UsernameNotFoundException("로그인 처리 중 오류가 발생했습니다.");
        }
    }
}