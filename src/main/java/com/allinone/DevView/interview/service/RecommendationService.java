package com.allinone.DevView.interview.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    @Qualifier("alan")
    private final ExternalAiApiService alan;

    public String getRecommendations(String keyword) {
        if (alan instanceof AlanApiService) {
            return ((AlanApiService) alan).getRecommendations(keyword);
        }

        return "Recommendation service is not configured correctly.";
    }
}
