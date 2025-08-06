package com.devview.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/** * 사용자 엔티티 - ERD 기준 단순 설계 */
@Entity
@Table(name = "Users")
@Getter
@Setter
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

    // ✅ [추가된 마이페이지 관련 필드들]
    @Column(name = "job", length = 50)
    private String job;

    @Column(name = "career_level", length = 50)
    private String careerLevel;

    @Column(name = "profile_image_url", length = 300)
    private String profileImageUrl;

    // ========================================    // 연관관계 매핑 (필요한 것만)    // ========================================

//    /**//     * 사용자 프로필 (1:1 관계)//     */
//    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private UserProfile userProfile;

//    /**//     * 작성한 커뮤니티 게시글들 (1:N 관계)//     */
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private List<CommunityPost> communityPosts;

//    /**//     * 참여한 인터뷰들 (1:N 관계)//     */
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private List<Interview> interviews;

//    /**//     * 좋아요한 게시글들 (1:N 관계)//     */
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private List<Like> likes;

//    /**//     * 스크랩한 게시글들 (1:N 관계)//     */
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private List<Scrap> scraps;

//    /**//     * 작성한 댓글들 (1:N 관계)//     */
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private List<Comment> comments;

//    /**//     * 랭킹 정보 (1:1 관계)//     */
//    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private Ranking ranking;

    // ========================================
    // 전용 생성자들 (정적 팩토리 메서드)
    // ========================================

    /**     * 로컬 회원가입 사용자 생성     */
    public static User createLocalUser(String username, String email, String encodedPassword) {
        return User.builder()
                .username(username)
                .email(email)
                .password(encodedPassword)
                .provider("LOCAL")
                .providerId(null)
                .build();
    }

    /**     * 구글 OAuth2 사용자 생성     */
    public static User createGoogleUser(String username, String email, String googleProviderId) {
        return User.builder()
                .username(username)
                .email(email)
                .password(null)
                .provider("GOOGLE")
                .providerId(googleProviderId)
                .build();
    }

    // ========================================    // 기본 유틸리티 메서드    // ========================================

    /**     * 구글 OAuth2 사용자인지 확인     */
    public boolean isGoogleUser() {
        return "GOOGLE".equals(this.provider);
    }

    /**     * 로컬 회원가입 사용자인지 확인     */
    public boolean isLocalUser() {
        return "LOCAL".equals(this.provider) || this.provider == null;
    }

    /**     * 비밀번호가 필요한 사용자인지 확인     */
    public boolean requiresPassword() {
        return isLocalUser();
    }

    // ✅ [선택적으로 제공되는 이름 setter → 이름을 username으로 처리]
    public void setName(String name) {
        this.username = name;
    }

    public String getName() {
        return this.username;
    }
}
