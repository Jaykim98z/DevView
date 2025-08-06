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
        String prompt = String.format(
                "You are an interviewer for a %s position targeting a %s developer. " +
                        "Please ask one single, clear, and essential technical question relevant to this role. " +
                        "Do not add any introductory or closing remarks, just the question itself.",
                jobPosition, careerLevel
        );

        return generateContent(prompt);
    }

    public String generateContent(String prompt) {
        GeminiRequestDto requestDto = GeminiRequestDto.from(prompt);
        String urlWithKey = API_URL + "?key=" + apiKey;

        GeminiResponseDto responseDto = restTemplate.postForObject(urlWithKey, requestDto, GeminiResponseDto.class);

        return responseDto != null ? responseDto.extractText() : "Failed to get a response.";
    }
}
