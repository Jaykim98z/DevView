package com.allinone.DevView.user.dto.response;

import com.allinone.DevView.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 사용자 정보 응답 DTO
 */
@Getter
@Builder
public class UserResponse {

    /**
     * 사용자 ID
     */
    private Long userId;

    /**
     * 사용자명
     */
    private String username;

    /**
     * 이메일
     */
    private String email;

    /**
     * 가입 방식 (LOCAL, GOOGLE)
     */
    private String provider;

    /**
     * 계정 생성일
     */
    private LocalDateTime createdAt;

    /**
     * User 엔티티를 UserResponse DTO로 변환하는 정적 메서드
     *
     * @param user User 엔티티
     * @return UserResponse DTO
     */
    public static UserResponse from(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .provider(user.getProvider())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * 로그인 인증 처리 (예시)
     */
    public static UserResponse authenticateUser(String email, String password) {
        if ("test@example.com".equals(email) && "password".equals(password)) {
            return UserResponse.builder()
                    .userId(1L)
                    .email(email)
                    .username("Test User")
                    .provider("LOCAL")
                    .createdAt(LocalDateTime.now())
                    .build();
        }
        return null;
    }
}
