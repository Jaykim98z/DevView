package com.allinone.DevView.mypage.dto;

import com.allinone.DevView.interview.entity.Grade;
import com.allinone.DevView.interview.entity.InterviewResult;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InterviewDto {

    private final Long interviewId;
    private final String interviewDate;
    private final String interviewType;
    private final int score;
    private final Grade grade;
    private final String feedback;

    public static InterviewDto fromEntity(InterviewResult result) {
        return InterviewDto.builder()
                .interviewId(result.getInterview().getId())
                .interviewDate(result.getInterview().getCreatedAt().toLocalDate().toString())
                .interviewType(result.getInterview().getInterviewType().name()) // 👈 .getName() → .name()
                .score(result.getTotalScore())
                .grade(result.getGrade())
                .feedback(result.getFeedback())
                .build();
    }
}
