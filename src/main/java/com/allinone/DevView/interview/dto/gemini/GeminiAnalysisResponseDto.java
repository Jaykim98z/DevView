package com.allinone.DevView.interview.dto.gemini;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GeminiAnalysisResponseDto(
        @JsonProperty("totalScore") int totalScore,
        @JsonProperty("summary") String summary,
        @JsonProperty("techScore") int techScore,
        @JsonProperty("problemScore") int problemScore,
        @JsonProperty("commScore") int commScore,
        @JsonProperty("attitudeScore") int attitudeScore,
        @JsonProperty("keywords") List<String> keywords,
        @JsonProperty("detailedFeedback") List<DetailedFeedbackItem> detailedFeedback
) {
    public static record DetailedFeedbackItem(
            @JsonProperty("question") String question,
            @JsonProperty("answer") String answer,
            @JsonProperty("feedback") String feedback
    ) {}
}
