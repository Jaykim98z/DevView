package com.allinone.DevView.interview.service;

import com.allinone.DevView.interview.dto.gemini.GeminiRequestDto;
import com.allinone.DevView.interview.dto.gemini.GeminiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service("gemini")
@RequiredArgsConstructor
public class GeminiApiService implements ExternalAiApiService {
    private final RestTemplate restTemplate;

    @Value("${gemini.api-key}")
    private String apiKey;

    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    @Override
    public List<String> getQuestionFromAi(String jobPosition, String careerLevel) {
        String prompt = String.format(
                "You are an interviewer for a %s position targeting a %s developer. " +
                        "Please generate exactly 5 distinct, essential technical questions for this role. " +
                        "Each question must be on a new line. Do not number them or add any other text.",
                jobPosition, careerLevel
        );

        String rawResponse = generateContent(prompt);

        return Arrays.stream(rawResponse.split("\n"))
                .filter(line -> !line.trim().isEmpty())
                .collect(Collectors.toList());
    }

    public String generateContent(String prompt) {
        GeminiRequestDto requestDto = GeminiRequestDto.from(prompt);
        String urlWithKey = API_URL + "?key=" + apiKey;

        GeminiResponseDto responseDto = restTemplate.postForObject(urlWithKey, requestDto, GeminiResponseDto.class);

        return responseDto != null ? responseDto.extractText() : "Failed to get a response.";
    }
}
