package com.allinone.DevView.interview.service;

import com.allinone.DevView.common.enums.InterviewType;

import java.util.List;

public interface ExternalAiApiService {
    List<String> getQuestionFromAi(String jobPosition, String careerLevel, int questionCount, InterviewType interviewType);
    String generateContent(String prompt);
}
