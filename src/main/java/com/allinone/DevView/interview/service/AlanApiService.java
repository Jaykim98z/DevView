package com.allinone.DevView.interview.service;

import com.allinone.DevView.interview.dto.alan.AlanResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service("alan")
@RequiredArgsConstructor
public class AlanApiService implements ExternalAiApiService {
    private final RestTemplate restTemplate;

    @Value("${alan.client-id}")
    private String clientId;

    private static final String API_URL = "https://kdt-api-function.azurewebsites.net/api/v1/question";

    @Override
    public String getQuestionFromAi(String jobPosition, String careerLevel) {
        // 이 메서드는 Gemini가 담당하므로 여기서는 간단히 구현
        return "This method is handled by Gemini Service.";
    }

    /**
     * Alan API를 사용하여 특정 키워드에 대한 추천 자료를 검색합니다.
     * @param keyword 검색할 키워드 (e.g., "JPA", "Spring Security")
     * @return 검색된 추천 자료 내용 (content 필드)
     */
    public String getRecommendations(String keyword) {
        String prompt = String.format(
                "Please provide a list of helpful resources (like blog posts, official docs, or tutorials) for learning about '%s'.",
                keyword
        );

        URI uri = UriComponentsBuilder.fromUriString(API_URL)
                .queryParam("client_id", clientId)
                .queryParam("content", prompt)
                .build()
                .toUri();

        AlanResponseDto response = restTemplate.getForObject(uri, AlanResponseDto.class);

        return response != null ? response.content() : "Failed to get recommendations.";
    }
}
