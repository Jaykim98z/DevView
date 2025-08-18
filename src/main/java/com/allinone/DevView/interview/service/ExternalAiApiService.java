package com.allinone.DevView.interview.service;

import java.util.List;

public interface ExternalAiApiService {
    List<String> getQuestionFromAi(String jobPosition, String careerLevel, int questionCount);
    String generateContent(String prompt);
}
