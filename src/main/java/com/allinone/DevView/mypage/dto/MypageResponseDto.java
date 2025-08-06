package com.allinone.DevView.mypage.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MypageResponseDto {

    // 👤 기본 프로필 정보
    private final String name;
    private final String email;
    private final String job;
    private final String careerLevel;
    private final String profileImageUrl;

    // 📌 계정 정보
    private final Long memberId;
    private final String joinedAt;

    // 📊 면접 요약
    private final int totalInterviews;
    private final int avgScore;
    private final String grade;

    // 📋 상세 리스트
    private final List<InterviewDto> interviews;
    private final List<ScrapDto> scraps;
}