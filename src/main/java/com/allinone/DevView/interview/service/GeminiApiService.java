package com.allinone.DevView.interview.service;

import com.allinone.DevView.interview.dto.gemini.GeminiRequestDto;
import com.allinone.DevView.interview.dto.gemini.GeminiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service("gemini")
@RequiredArgsConstructor
public class GeminiApiService implements ExternalAiApiService {
    private final RestTemplate restTemplate;

    @Value("${gemini.api-key}")
    private String apiKey;

    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    @Override
    public String getQuestionFromAi(String jobPosition, String careerLevel) {
        // 1. 프롬프트 생성
        String prompt = String.format(
                "You are an interviewer for a %s position targeting a %s developer. " +
                        "Please ask one single, clear, and essential technical question relevant to this role. " +
                        "Do not add any introductory or closing remarks, just the question itself.",
                jobPosition, careerLevel
        );

        // 2. Gemini API에 보낼 요청 객체 생성
        GeminiRequestDto requestDto = GeminiRequestDto.from(prompt);
        String urlWithKey = API_URL + "?key=" + apiKey;

        // 3. API 호출 및 응답 받기
        GeminiResponseDto responseDto = restTemplate.postForObject(urlWithKey, requestDto, GeminiResponseDto.class);

        // 4. 응답에서 텍스트 추출하여 반환
        return responseDto != null ? responseDto.extractText() : "Failed to get a response.";
    }
}
