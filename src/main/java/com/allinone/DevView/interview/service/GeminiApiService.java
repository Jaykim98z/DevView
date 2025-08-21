package com.allinone.DevView.interview.service;

import com.allinone.DevView.common.enums.InterviewType;
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
    public List<String> getQuestionFromAi(String jobPosition, String careerLevel, int questionCount, InterviewType interviewType, String selfIntroduction) {
        String prompt = String.format(
                "You are an interviewer. Please generate exactly %d distinct '%s' questions for a %s position targeting a %s developer. " +
                        (selfIntroduction != null && !selfIntroduction.trim().isEmpty() ?
                                "The candidate has this background: \"" + selfIntroduction + "\". Please consider their experience when generating questions. " : "") +
                        "CRITICAL: Your response must only contain the questions. Each question must be on a new line. " +
                        "Do NOT include any numbers, bullet points, markdown formatting, or any extra text. " +
                        "For example, a valid response for 2 questions would be:\n" +
                        "What is the difference between a class and an object?\n" +
                        "Explain the concept of RESTful APIs.\n\n" +
                        "Please generate the questions in Korean.",
                questionCount, interviewType.toString(), jobPosition, careerLevel
        );

        String rawResponse = generateContent(prompt);

        return Arrays.stream(rawResponse.split("\n"))
                .map(line -> line.replaceAll("^\\s*[-*]?\\s*\\d*\\.\\s*", "").trim())
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
