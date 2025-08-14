package com.allinone.DevView.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import com.allinone.DevView.mypage.entity.UserProfile;

/**
 * 사용자 엔티티 - ERD 기준 단순 설계
 */
@Entity
@Table(name = "Users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"password", "userProfile"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", nullable = false, length = 20)
    private String username;

    @Column(name = "password", nullable = true, length = 250)
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

    /**
     * 사용자 프로필 (1:1 관계)
     * - 연관관계 주인: UserProfile.user (FK: user_id)
     * - 캐스케이드: User 저장/삭제 시 프로필도 함께 영속성 전이
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    private UserProfile userProfile;

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

    public boolean isGoogleUser() {
        return "GOOGLE".equals(this.provider);
    }

    public boolean isLocalUser() {
        return "LOCAL".equals(this.provider) || this.provider == null;
    }

    public boolean requiresPassword() {
        return isLocalUser();
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
        if (userProfile != null && userProfile.getUser() != this) {
            userProfile.setUser(this);
        }
    }
}
