package com.allinone.DevView.interview.dto.request;

import com.allinone.DevView.interview.entity.InterviewType;
import lombok.Getter;

@Getter
public class StartInterviewRequest {
    private Long userId;
    private InterviewType interviewType;
    private String jobPosition;
    private String careerLevel;
}
