// src/main/java/com/allinone/DevView/mypage/dto/InterviewDto.java
package com.allinone.DevView.mypage.dto;

import com.allinone.DevView.interview.entity.Grade;
import com.allinone.DevView.interview.entity.Interview;
import com.allinone.DevView.interview.entity.InterviewResult;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

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
        Interview i = result.getInterview();
        LocalDateTime when = (i.getEndedAt() != null ? i.getEndedAt() : i.getCreatedAt());
        return InterviewDto.builder()
                .interviewId(i.getId())
                .interviewDate(when.toLocalDate().toString())
                .interviewType(i.getInterviewType().name())
                .score(result.getTotalScore())
                .grade(result.getGrade())
                .feedback(result.getFeedback())
                .build();
    }
}
