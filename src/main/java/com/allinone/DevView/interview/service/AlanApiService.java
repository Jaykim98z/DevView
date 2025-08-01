package com.allinone.DevView.interview.service;

import org.springframework.stereotype.Service;

@Service("alan")
public class AlanApiService implements ExternalAiApiService {

    @Override
    public String getQuestionFromAi(String jobPosition, String careerLevel) {
        // Alan API 호출 로직
        return "Question from Alan...";
    }
}
