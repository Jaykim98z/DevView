package com.allinone.DevView.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "Users")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"password"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", nullable = false, length = 20)
    private String username;

    @Column(name = "password", nullable = false, length = 250)
    private String password;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "provider", length = 100)
    private String provider; // "LOCAL" 또는 "GOOGLE"

    @Column(name = "provider_id", length = 100)
    private String providerId; // 구글 OAuth2 사용자 ID

    // ========================================
    // 추가된 필드 (job, careerLevel, profileImageUrl)
    // ========================================

    @Column(name = "job", length = 100)
    private String job;

    @Column(name = "career_level", length = 100)
    private String careerLevel;

    @Column(name = "profile_image_url", length = 255)
    private String profileImageUrl;

    // ========================================
    // 추가된 Getter 메서드
    // ========================================
    // ID 값을 반환하는 메서드
    public Long getId() {
        return this.userId;
    }

    // 사용자 권한을 반환하는 메서드 (단일 권한 예시로 "ROLE_USER")
    public List<GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    // ========================================
    // 전용 생성자들 (정적 팩토리 메서드)
    // ========================================

    /**
     * 로컬 회원가입 사용자 생성
     */
    public static User createLocalUser(String username, String email, String encodedPassword) {
        return User.builder()
                .username(username)
                .email(email)
                .password(encodedPassword)
                .provider("LOCAL")
                .providerId(null)  // 로컬 사용자는 providerId 없음
                .build();
    }

    /**
     * 구글 OAuth2 사용자 생성
     */
    public static User createGoogleUser(String username, String email, String googleProviderId) {
        return User.builder()
                .username(username)
                .email(email)
                .password(null)  // OAuth2 사용자는 패스워드 없음
                .provider("GOOGLE")
                .providerId(googleProviderId)
                .build();
    }

    // ========================================
    // 기본 유틸리티 메서드
    // ========================================
    /**
     * 구글 OAuth2 사용자인지 확인
     */
    public boolean isGoogleUser() {
        return "GOOGLE".equals(this.provider);
    }

    /**
     * 로컬 회원가입 사용자인지 확인
     */
    public boolean isLocalUser() {
        return "LOCAL".equals(this.provider) || this.provider == null;
    }

    /**
     * 비밀번호가 필요한 사용자인지 확인
     */
    public boolean requiresPassword() {
        return isLocalUser();
    }

    // ========================================
    // 추가된 setter 메서드
    // ========================================
    public void setJob(String job) {
        this.job = job;
    }

    public void setCareerLevel(String careerLevel) {
        this.careerLevel = careerLevel;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void setName(String name) {
        this.username = name;  // 'name'을 'username'으로 매핑
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return this.username;
    }
}
