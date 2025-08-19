package com.allinone.DevView.mypage.entity;

import com.allinone.DevView.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * 사용자 프로필 확장 정보 (마이페이지 전용)
 * - User 엔티티와 1:1 매핑
 * - 직무, 경력 수준, 프로필 이미지 등 UI 표시용 데이터 저장
 */
@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long id;

    /** User와 1:1 연결 (FK: user_id) */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /** 직무 (예: 백엔드, 프론트엔드, AI 등) */
    @Column(name = "job_position", length = 100)
    private String job;

    /** 경력 수준 (예: 신입, 주니어, 시니어 등) */
    @Column(name = "career_level", length = 50)
    private String careerLevel;

    /** 프로필 이미지 URL (저장 경로나 S3 URL 등) */
    @Column(name = "profile_image_url", length = 255)
    private String profileImageUrl;

    /** 자기소개 (AI 면접 질문 개인화에 활용, 프론트엔드에서 한글 200자 제한) */
    @Column(name = "self_introduction", length = 1000)
    private String selfIntroduction;
}
