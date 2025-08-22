package com.allinone.DevView.interview.dto.gemini;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for constructing the request body for the Gemini API.
 */
public record GeminiRequestDto(List<Content> contents) {
    public static record Content(List<Part> parts) {}
    public static record Part(String text) {}

    /**
     * Creates a GeminiRequestDto from a simple text prompt.
     * @param prompt The text prompt to send to the AI.
     * @return A new GeminiRequestDto instance.
     */
    public static GeminiRequestDto from(String prompt) {
        Part part = new Part(prompt);
        Content content = new Content(new ArrayList<>(List.of(part)));
        return new GeminiRequestDto(new ArrayList<>(List.of(content)));
    }
}
