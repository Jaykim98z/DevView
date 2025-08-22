package com.allinone.DevView.interview.dto.response;

import com.allinone.DevView.common.enums.CareerLevel;
import com.allinone.DevView.common.enums.InterviewType;
import com.allinone.DevView.common.enums.JobPosition;
import com.allinone.DevView.interview.entity.Interview;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO for responding with the initial details of a newly created interview session.
 */
@Getter
@Builder
public class InterviewResponse {
    private Long interviewId;
    private InterviewType interviewType;
    private JobPosition jobPosition;
    private CareerLevel careerLevel;

    /**
     * Creates an InterviewResponse DTO from an Interview entity.
     * @param interview The entity to convert.
     * @return A new InterviewResponse instance.
     */
    public static InterviewResponse fromEntity(Interview interview) {
        return InterviewResponse.builder()
                .interviewId(interview.getId())
                .interviewType(interview.getInterviewType())
                .jobPosition(interview.getJobPosition())
                .careerLevel(interview.getCareerLevel())
                .build();
    }
}
