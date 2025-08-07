package com.allinone.DevView.interview.service;

public interface ExternalAiApiService {
    String getQuestionFromAi(String jobPosition, String careerLevel);
    String generateContent(String prompt);
}
