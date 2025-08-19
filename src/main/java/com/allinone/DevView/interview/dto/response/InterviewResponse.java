package com.allinone.DevView.interview.dto.response;

import com.allinone.DevView.common.enums.InterviewType;
import com.allinone.DevView.interview.entity.Interview;
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
                .interviewType(interview.getInterviewType())
                .jobPosition(interview.getJobPosition())
                .build();
    }
}
