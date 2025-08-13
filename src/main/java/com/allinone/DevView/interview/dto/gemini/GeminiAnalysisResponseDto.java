package com.allinone.DevView.interview.dto.gemini;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GeminiAnalysisResponseDto(
        @JsonProperty("totalScore") int totalScore,
        @JsonProperty("feedback") String feedback,
        @JsonProperty("summary") String summary,
        @JsonProperty("techScore") int techScore,
        @JsonProperty("problemScore") int problemScore,
        @JsonProperty("commScore") int commScore,
        @JsonProperty("attitudeScore") int attitudeScore
) {}
