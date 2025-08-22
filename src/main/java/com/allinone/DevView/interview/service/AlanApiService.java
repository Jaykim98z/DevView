package com.allinone.DevView.interview.service;

import com.allinone.DevView.common.enums.InterviewType;
import com.allinone.DevView.interview.dto.alan.AlanResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("alan")
@RequiredArgsConstructor
public class AlanApiService implements ExternalAiApiService {
    private final RestTemplate restTemplate;

    @Value("${alan.client-id}")
    private String clientId;

    private static final String API_URL = "https://kdt-api-function.azurewebsites.net/api/v1/question";

    @Override
    public List<String> getQuestionFromAi(String jobPosition, String careerLevel, int questionCount, InterviewType interviewType, String selfIntroduction) {
        // 이 메서드는 Gemini가 담당하므로 여기서는 간단히 구현
        return null;
    }

    /**
     * Alan API를 사용하여 특정 키워드에 대한 추천 리소스를 검색합니다.
     * @param keyword 검색할 키워드(예: "JPA", "커뮤니케이션 스킬")
     * @return API 응답의 content 필드
     */
    public String getRecommendations(String keyword) {
        String prompt = "For the following comma-separated topics: '" + keyword + "', " +
                "Please provide a list of 1 to 5 helpful, REAL, and publicly accessible learning resources for EACH topic. (like well-known blog posts, official documentation, or popular tutorials) " +
                "CRITICAL: DO NOT invent sources, NEVER use placeholder URLs like 'example.com'. (Use the actual results found by searching) " +
                "Your response MUST be a single, valid JSON object where each key is one of the topics, its value is an array of objects. " +
                "Each object in the array must have two keys: 'title' (string) and 'url' (string). ";

        return generateContent(prompt);
    }

    /**
     * 주어진 프롬프트를 Alan API로 전송하고 AI의 전체 텍스트 응답을 반환합니다.
     * @param prompt Alan API에 보낼 전체 프롬프트
     * @return API 응답의 원시 텍스트 내용
     */
    @Override
    public String generateContent(String prompt) {
        String urlTemplate = API_URL + "?client_id={clientId}&content={content}";

        Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put("clientId", clientId);
        uriVariables.put("content", prompt);

        AlanResponseDto response = restTemplate.getForObject(urlTemplate, AlanResponseDto.class, uriVariables);

        return response != null ? response.content() : "Failed to get response from Alan.";
    }
}
