package com.allinone.DevView.interview.dto.response;

import com.allinone.DevView.interview.entity.Interview;
import com.allinone.DevView.interview.entity.InterviewType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InterviewResponse {
    private Long interviewId;
    private InterviewType interviewType;
    private String jobPosition;

    public static InterviewResponse fromEntity(Interview interview) {
        return InterviewResponse.builder()
                .interviewId(interview.getId())
                // String → Enum 변환 (대문자 일치 필요)
                .interviewType(
                        interview.getInterviewType() != null
                                ? InterviewType.valueOf(interview.getInterviewType().toUpperCase())
                                : null
                )
                .jobPosition(interview.getJobPosition())
                .build();
    }
}
