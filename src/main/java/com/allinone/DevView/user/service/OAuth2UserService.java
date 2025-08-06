package com.allinone.DevView.user.service;

import com.allinone.DevView.user.dto.response.UserResponse;
import com.allinone.DevView.user.entity.User;
import com.allinone.DevView.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
        return userRepository.findByEmail(email)
                .map(UserResponse::from)
                .orElseGet(() -> {
                    User newUser = User.createGoogleUser(name, email, providerId);
                    return UserResponse.from(userRepository.save(newUser));
                });
    }
}
