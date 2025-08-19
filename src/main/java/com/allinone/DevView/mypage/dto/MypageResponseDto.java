package com.allinone.DevView.mypage.dto;

import com.allinone.DevView.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class MypageResponseDto {

    // 👤 기본 프로필 정보
    private final String name;
    private final String email;
    private final String job;
    private final String careerLevel;
    private String profileImageUrl;
    private final String selfIntroduction; //자기소개

    // 📌 계정 정보
    private final Long memberId;
    private LocalDateTime joinedAt;

    // 📊 면접 요약
    private final int totalInterviews;
    private final int avgScore;
    private final String grade;

    // 📋 상세 리스트
    private final List<InterviewDto> interviews;
    private final List<ScrapDto> scraps;

    public static MypageResponseDto from(User user,
                                         int totalInterviews,
                                         int avgScore,
                                         String grade,
                                         List<InterviewDto> interviews,
                                         List<ScrapDto> scraps) {

        String job = null;
        String careerLevel = null;
        String profileImageUrl = null;
        String selfIntroduction = null;

        if (user.getUserProfile() != null) {
            job = user.getUserProfile().getJob();
            careerLevel = user.getUserProfile().getCareerLevel();
            profileImageUrl = user.getUserProfile().getProfileImageUrl();
            selfIntroduction = user.getUserProfile().getSelfIntroduction();
        }

        return MypageResponseDto.builder()
                .name(user.getUsername())
                .email(user.getEmail())
                .job(job)
                .careerLevel(careerLevel)
                .profileImageUrl(profileImageUrl)
                .selfIntroduction(selfIntroduction)
                .memberId(user.getUserId())
                .joinedAt(user.getCreatedAt() != null ? user.getCreatedAt() : null)
                .totalInterviews(totalInterviews)
                .avgScore(avgScore)
                .grade(grade)
                .interviews(interviews)
                .scraps(scraps)
                .build();
    }
}