package com.allinone.DevView.mypage.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InterviewDto {
    private final Long interviewId;
    private final String interviewDate;
    private final String interviewType;
    private final int score;
    private final String feedback;
}
