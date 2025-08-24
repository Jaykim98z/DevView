package com.allinone.DevView.interview.service;

import com.allinone.DevView.common.enums.InterviewType;

import java.util.List;

/**
 * Defines the contract for external AI services (like Gemini or Alan).
 * This allows the application to interact with different AI providers
 * through a consistent interface.
 */
public interface ExternalAiApiService {

    /**
     * Generates a list of interview questions based on specified criteria.
     * @param jobPosition The job position for the interview.
     * @param careerLevel The candidate's career level.
     * @param questionCount The number of questions to generate.
     * @param interviewType The type of interview.
     * @param selfIntroduction The user's self-introduction text.
     * @return A list of generated question strings.
     */
    List<String> getQuestionFromAi(String jobPosition, String careerLevel, int questionCount, InterviewType interviewType, String selfIntroduction);

    /**
     * Sends a generic prompt to the AI and gets a raw text response.
     * @param prompt The full prompt to send to the AI.
     * @return The AI's generated text content.
     */
    String generateContent(String prompt);
}
