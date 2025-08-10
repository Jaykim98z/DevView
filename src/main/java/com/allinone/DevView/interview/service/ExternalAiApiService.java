package com.allinone.DevView.interview.service;

import java.util.List;

public interface ExternalAiApiService {
    List<String> getQuestionFromAi(String jobPosition, String careerLevel);
    String generateContent(String prompt);
}
