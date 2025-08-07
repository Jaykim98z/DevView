package com.allinone.DevView.user.service;

import com.allinone.DevView.user.dto.response.UserResponse;
import com.allinone.DevView.user.entity.User;
import com.allinone.DevView.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * OAuth2 로그인 사용자를 처리하는 서비스
 * - 이메일 기준으로 사용자 조회
 * - 없으면 새로 생성
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OAuth2UserService {

    private final UserRepository userRepository;

    /**
     * OAuth2 사용자 정보를 처리 (조회 또는 신규 생성)
     * @param email OAuth2 이메일
     * @param name OAuth2 이름
     * @param providerId 소셜 고유 ID
     * @return UserResponse DTO
     */
    public UserResponse handleOAuth2User(String email, String name, String providerId) {
        log.info("OAuth2 사용자 처리 시작: email={}, name={}", email, name);

        try {
            return userRepository.findByEmail(email)
                    .map(existingUser -> {
                        log.info("기존 OAuth2 사용자 찾음: userId={}, email={}",
                                existingUser.getUserId(), existingUser.getEmail());
                        return UserResponse.from(existingUser);
                    })
                    .orElseGet(() -> {
                        log.info("신규 OAuth2 사용자 생성 시도: email={}, name={}", email, name);

                        User newUser = User.createGoogleUser(name, email, providerId);
                        User savedUser = userRepository.save(newUser);

                        log.info("신규 OAuth2 사용자 저장 성공: userId={}, email={}",
                                savedUser.getUserId(), savedUser.getEmail());

                        return UserResponse.from(savedUser);
                    });
        } catch (Exception e) {
            log.error("OAuth2 사용자 처리 중 오류 발생: email={}", email, e);
            throw new RuntimeException("OAuth2 사용자 처리 실패", e);
        }
    }
}