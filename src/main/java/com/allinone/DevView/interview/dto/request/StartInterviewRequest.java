package com.allinone.DevView.interview.dto.request;

import com.allinone.DevView.common.enums.InterviewType;
import lombok.Getter;

@Getter
public class StartInterviewRequest {
    private Long userId;
    private InterviewType interviewType;
    private String jobPosition;
    private String careerLevel;
    private int questionCount;
    private int durationMinutes;
}
