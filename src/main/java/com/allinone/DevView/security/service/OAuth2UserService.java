package com.allinone.DevView.security.service;

import com.allinone.DevView.user.dto.response.UserResponse;
import com.allinone.DevView.user.entity.User;
import com.allinone.DevView.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * OAuth2 로그인 사용자를 처리하는 서비스
 * ✅ 개선사항: provider_id 기준으로 사용자 조회하여 LOCAL 사용자와 완전 분리
 * - GOOGLE 사용자는 provider_id로 정확히 식별
 * - 같은 이메일의 LOCAL 사용자와 충돌 방지
 * - 구글 고유 ID로 중복 생성 방지
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OAuth2UserService {

    private final UserRepository userRepository;

    /**
     * OAuth2 사용자 정보를 처리 (조회 또는 신규 생성)
     *
     * 처리 순서:
     * 1. provider_id로 기존 GOOGLE 사용자 찾기
     * 2. 없으면 새로운 GOOGLE 사용자 생성
     * 3. 이메일 변경 감지 시 업데이트
     * 4. 사용자명 중복 방지 처리
     *
     * @param email OAuth2 이메일
     * @param name OAuth2 이름
     * @param providerId 구글 고유 ID (sub)
     * @return UserResponse DTO
     */
    public UserResponse handleOAuth2User(String email, String name, String providerId) {
        log.info("OAuth2 사용자 처리 시작: email={}, name={}, providerId={}", email, name, providerId);

        // 입력값 검증
        if (providerId == null || providerId.trim().isEmpty()) {
            log.error("OAuth2 provider_id가 없습니다: email={}, name={}", email, name);
            throw new IllegalArgumentException("OAuth2 provider_id는 필수입니다.");
        }

        try {
            // 1. provider_id로 기존 구글 사용자 조회 (정확한 방법)
            return userRepository.findByProviderAndProviderId("GOOGLE", providerId)
                    .map(existingUser -> {
                        log.info("기존 구글 사용자 발견: userId={}, email={}, providerId={}",
                                existingUser.getUserId(), existingUser.getEmail(), providerId);

                        // 이메일 변경 감지 및 업데이트
                        return updateUserEmailIfChanged(existingUser, email);
                    })
                    .orElseGet(() -> {
                        // 2. 기존 구글 사용자가 없으면 새로 생성
                        log.info("신규 구글 사용자 생성: email={}, name={}, providerId={}",
                                email, name, providerId);

                        return createNewGoogleUser(email, name, providerId);
                    });

        } catch (Exception e) {
            log.error("OAuth2 사용자 처리 중 오류 발생: email={}, providerId={}", email, providerId, e);
            throw new RuntimeException("OAuth2 사용자 처리 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 기존 사용자의 이메일 변경 감지 및 업데이트
     *
     * @param existingUser 기존 사용자
     * @param newEmail 새로운 이메일
     * @return UserResponse
     */
    private UserResponse updateUserEmailIfChanged(User existingUser, String newEmail) {
        if (!existingUser.getEmail().equals(newEmail)) {
            log.info("구글 사용자 이메일 변경 감지: userId={}, {} -> {}",
                    existingUser.getUserId(), existingUser.getEmail(), newEmail);

            // 이메일 변경 시 충돌 확인
            if (userRepository.existsByEmailAndProvider(newEmail, "GOOGLE")) {
                log.warn("이메일 변경 실패 - 이미 사용 중: email={}, providerId={}",
                        newEmail, existingUser.getProviderId());
                // 기존 이메일 유지
            } else {
                existingUser.setEmail(newEmail);
                userRepository.save(existingUser);
                log.info("구글 사용자 이메일 업데이트 완료: userId={}, newEmail={}",
                        existingUser.getUserId(), newEmail);
            }
        }

        return UserResponse.from(existingUser);
    }

    /**
     * 새로운 구글 사용자 생성
     *
     * @param email 이메일
     * @param name 이름
     * @param providerId 구글 고유 ID
     * @return UserResponse
     */
    private UserResponse createNewGoogleUser(String email, String name, String providerId) {
        // 1. 사용자명 중복 방지 처리
        String uniqueUsername = generateUniqueUsername(name);

        // 2. 구글 사용자 생성
        User newUser = User.createGoogleUser(uniqueUsername, email, providerId);
        User savedUser = userRepository.save(newUser);

        log.info("신규 구글 사용자 저장 성공: userId={}, email={}, username={}, providerId={}",
                savedUser.getUserId(), savedUser.getEmail(), savedUser.getUsername(), providerId);

        return UserResponse.from(savedUser);
    }

    /**
     * 사용자명 중복 방지를 위한 고유 사용자명 생성
     *
     * 생성 규칙:
     * 1. 원본 이름 사용 시도
     * 2. 중복 시 숫자 접미사 추가 (김개발1, 김개발2...)
     * 3. 최대 999까지 시도 후 타임스탬프 사용
     *
     * @param originalName 원본 이름
     * @return 중복되지 않는 고유 사용자명
     */
    private String generateUniqueUsername(String originalName) {
        if (originalName == null || originalName.trim().isEmpty()) {
            originalName = "구글사용자";
        }

        String baseUsername = originalName.trim();
        String uniqueUsername = baseUsername;
        int suffix = 1;

        // 이름이 중복되면 숫자 접미사 추가
        while (userRepository.existsByUsername(uniqueUsername)) {
            uniqueUsername = baseUsername + suffix;
            suffix++;

            // 무한루프 방지 (최대 999까지)
            if (suffix > 999) {
                uniqueUsername = baseUsername + "_" + System.currentTimeMillis();
                log.warn("사용자명 중복이 너무 많아 타임스탬프 사용: {}", uniqueUsername);
                break;
            }
        }

        if (!uniqueUsername.equals(baseUsername)) {
            log.info("사용자명 중복으로 변경: {} -> {}", baseUsername, uniqueUsername);
        }

        return uniqueUsername;
    }
}