package com.allinone.DevView.interview.service;

import com.allinone.DevView.common.enums.InterviewType;
import com.allinone.DevView.interview.dto.gemini.GeminiRequestDto;
import com.allinone.DevView.interview.dto.gemini.GeminiResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An implementation of the ExternalAiApiService that communicates with the Google Gemini AI.
 * This service is responsible for generating interview questions and analyzing results.
 * It is registered with the qualifier "gemini".
 */
@Slf4j
@Service("gemini")
@RequiredArgsConstructor
public class GeminiApiService implements ExternalAiApiService {
    private final RestTemplate restTemplate;

    @Value("${gemini.api-key}")
    private String apiKey;

    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    /**
     * Generates a list of interview questions tailored to the user's profile and settings.
     * It dynamically adjusts the prompt based on the user's self-introduction.
     * @param jobPosition The job position for the interview.
     * @param careerLevel The candidate's career level.
     * @param questionCount The number of questions to generate.
     * @param interviewType The type of interview.
     * @param selfIntroduction The user's self-introduction text (can be null or empty).
     * @return A list of generated question strings, with a greeting added to the first question.
     */
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

        List<String> questions = Arrays.stream(rawResponse.split("\n"))
                .map(line -> line.replaceAll("^\\s*[-*]?\\s*\\d*\\.\\s*", "").trim())
                .filter(line -> !line.trim().isEmpty())
                .collect(Collectors.toList());

        // ✅ 첫 번째 질문에 인사말 추가
        if (!questions.isEmpty()) {
            String greeting = getRandomGreeting();
            questions.set(0, greeting + "\n\n" + questions.get(0));
        }

        return questions;
    }

    /**
     * Sends a generic prompt to the Gemini API and returns the AI's raw text response.
     * @param prompt The full prompt to send to the AI.
     * @return The AI's generated text content.
     */
    public String generateContent(String prompt) {
        GeminiRequestDto requestDto = GeminiRequestDto.from(prompt);
        String urlWithKey = API_URL + "?key=" + apiKey;

        GeminiResponseDto responseDto = restTemplate.postForObject(urlWithKey, requestDto, GeminiResponseDto.class);

        return responseDto != null ? responseDto.extractText() : "Failed to get a response.";
    }

    /**
     * ✅ 랜덤 인사말 선택
     * @return
     */
    private String getRandomGreeting() {
        String[] greetings = {
                "안녕하세요. 오늘 인터뷰에 참여해주셔서 감사합니다. 부담 갖지 마시고 편안하게 답변해주시면 됩니다. 그럼 첫 번째 질문부터 시작하겠습니다.",
                "반갑습니다! 오늘은 가볍게 대화한다는 느낌으로 진행하려고 해요. 긴장하지 마시고 솔직하게 말씀해주시면 됩니다. 바로 첫 번째 질문 드릴게요.",
                "와주셔서 감사합니다. 이번 시간은 지원자님의 생각과 경험을 알아가는 자리니까 너무 딱딱하게 생각하지 않으셔도 돼요. 그럼 첫 번째 질문부터 시작하겠습니다.",
                "안녕하세요! 오늘 면접 자리에 함께해주셔서 정말 고맙습니다. 편안한 마음으로 대화하는 느낌으로 진행해보려고 해요. 첫 번째 질문 시작할게요.",
                "반갑습니다. 오늘은 서로를 알아가는 소중한 시간이니까 너무 긴장하지 마시고 자연스럽게 답변해주시면 됩니다. 그럼 바로 시작하겠습니다."
        };

        int randomIndex = (int) (Math.random() * greetings.length);
        return greetings[randomIndex];
    }
}