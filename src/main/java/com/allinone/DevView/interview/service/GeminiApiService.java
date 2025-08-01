package com.allinone.DevView.interview.service;

import org.springframework.stereotype.Service;

@Service("gemini")
public class GeminiApiService implements ExternalAiApiService {

    @Override
    public String getQuestionFromAi(String jobPosition, String careerLevel) {
        // Gemini API 호출 로직
        return "Question from Gemini...";
    }
}
