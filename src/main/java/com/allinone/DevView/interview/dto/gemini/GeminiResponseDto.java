package com.allinone.DevView.interview.dto.gemini;

import java.util.List;

public record GeminiResponseDto(List<Candidate> candidates) {
    public static record Candidate(Content content) {}
    public static record Content(List<Part> parts, String role) {}
    public static record Part(String text) {}

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
