package com.allinone.DevView.interview.dto.response;

import com.allinone.DevView.interview.entity.InterviewResult;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class InterviewResultSummaryDto {
    private Long resultId;
    private String interviewType;
    private String jobPosition;
    private Integer totalScore;
    private String grade;
    private LocalDateTime createdAt;

    public static InterviewResultSummaryDto fromEntity(InterviewResult r) {
        if (r == null || r.getInterview() == null) return null;
        return InterviewResultSummaryDto.builder()
                .resultId(r.getId())
                .interviewType(r.getInterview().getInterviewType() != null
                        ? r.getInterview().getInterviewType().name() : null)
                .jobPosition(r.getInterview().getJobPosition())
                .totalScore(r.getTotalScore())
                .grade(r.getGrade() != null ? r.getGrade().name() : null)
                // InterviewResult에는 createdAt 없음 → Interview.createdAt 사용
                .createdAt(r.getInterview().getCreatedAt())
                .build();
    }
}
