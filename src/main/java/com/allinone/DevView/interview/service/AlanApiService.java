package com.allinone.DevView.interview.service;

import com.allinone.DevView.interview.dto.alan.AlanResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Service("alan")
@RequiredArgsConstructor
public class AlanApiService implements ExternalAiApiService {
    private final RestTemplate restTemplate;

    @Value("${alan.client-id}")
    private String clientId;

    private static final String API_URL = "https://kdt-api-function.azurewebsites.net/api/v1/question";

    @Override
    public List<String> getQuestionFromAi(String jobPosition, String careerLevel) {
        // 이 메서드는 Gemini가 담당하므로 여기서는 간단히 구현
        return null;
    }

    /**
     * Alan API를 사용하여 특정 키워드에 대한 추천 자료를 검색합니다.
     * @param keyword 검색할 키워드 (e.g., "JPA", "Spring Security")
     * @return 검색된 추천 자료 내용 (content 필드)
     */
    public String getRecommendations(String keyword) {
        String prompt = "Please provide a list of helpful learning resources for '" + keyword + "'. " +
                "Your response MUST be a single, valid JSON object with one key: 'recommendations'. " +
                "The value should be an array of objects, where each object has two keys: 'title' (string) and 'url' (string).";

        return generateContent(prompt);
    }

    /**
     * 인터페이스 요구사항을 만족시키는 범용 메서드
     * @param prompt Alan API에 보낼 전체 프롬프트
     * @return API 응답의 content 필드
     */
    @Override
    public String generateContent(String prompt) {
        URI uri = UriComponentsBuilder.fromUriString(API_URL)
                .queryParam("client_id", clientId)
                .queryParam("content", prompt)
                .build()
                .toUri();

        AlanResponseDto response = restTemplate.getForObject(uri, AlanResponseDto.class);

        return response != null ? response.content() : "Failed to get response from Alan.";
    }
}
