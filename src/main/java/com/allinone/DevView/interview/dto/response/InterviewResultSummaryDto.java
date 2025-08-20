package com.allinone.DevView.interview.dto.response;

import com.allinone.DevView.common.enums.JobPosition;
import com.allinone.DevView.interview.entity.InterviewResult;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class InterviewResultSummaryDto {

    private Long resultId;
    private String interviewType;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private JobPosition jobPosition;

    private Integer totalScore;
    private String grade;
    private LocalDateTime createdAt;

    public static InterviewResultSummaryDto fromEntity(InterviewResult r) {
        if (r == null || r.getInterview() == null) return null;

        return InterviewResultSummaryDto.builder()
                .resultId(r.getId())
                .interviewType(r.getInterview().getInterviewType() != null
                        ? r.getInterview().getInterviewType().name() : null)

                .jobPosition(JobPosition.valueOf(r.getInterview().getJobPosition()))
                .totalScore(r.getTotalScore())
                .grade(r.getGrade() != null ? r.getGrade().name() : null)

                .createdAt(r.getInterview().getCreatedAt())
                .build();
    }
}
