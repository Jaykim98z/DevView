package com.allinone.DevView.interview.dto.response;

import com.allinone.DevView.interview.entity.Grade;
import com.allinone.DevView.interview.entity.InterviewResult;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InterviewResultResponse {
    private Long resultId;
    private Long interviewId;
    private int totalScore;
    private Grade grade;
    private String feedback;

    public static InterviewResultResponse fromEntity(InterviewResult result) {
        return InterviewResultResponse.builder()
                .resultId(result.getId())
                .interviewId(result.getInterview().getId())
                .totalScore(result.getTotalScore())
                .grade(result.getGrade())
                .feedback(result.getFeedback())
                .build();
    }
}
