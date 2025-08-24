package com.allinone.DevView.interview.dto.gemini;

import java.util.List;

/**
 * DTO for mapping the general content generation response from the Gemini API.
 */
public record GeminiResponseDto(List<Candidate> candidates) {
    public static record Candidate(Content content) {}
    public static record Content(List<Part> parts, String role) {}
    public static record Part(String text) {}

    /**
     * Extracts the primary text content from the first candidate in the AI's response.
     * @return The generated text, or a default message if not found.
     */
    public String extractText() {
        if (this.candidates != null && !this.candidates.isEmpty()) {
            Candidate firstCandidate = this.candidates.get(0);

            if (firstCandidate.content() != null && !firstCandidate.content().parts().isEmpty()) {
                return firstCandidate.content().parts().get(0).text();
            }
        }

        return "Sorry, I couldn't generate a question.";
    }
}
