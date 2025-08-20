package com.allinone.DevView.interview.dto.request;

import com.allinone.DevView.common.enums.CareerLevel;
import com.allinone.DevView.common.enums.InterviewType;
import com.allinone.DevView.common.enums.JobPosition;
import lombok.Getter;

@Getter
public class StartInterviewRequest {
    private Long userId;
    private InterviewType interviewType;
    private JobPosition jobPosition;
    private CareerLevel careerLevel;
    private int questionCount;
    private int durationMinutes;
}
