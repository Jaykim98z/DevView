package com.allinone.DevView.interview.service;

import com.allinone.DevView.interview.dto.alan.AlanRecommendationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    @Qualifier("alan")
    private final ExternalAiApiService alan;
    private final ObjectMapper objectMapper;

    public String getRecommendations(String keyword) {
        if (alan instanceof AlanApiService) {
            return ((AlanApiService) alan).getRecommendations(keyword);
        }

        return "Recommendation service is not configured correctly.";
    }

    public String getSingleRecommendationHtml(String keyword) {
        try {
            String alanJson = ((AlanApiService) alan).getRecommendations(keyword);
            String cleanedAlanJson = alanJson.trim().replace("```json", "").replace("```", "").trim();
            AlanRecommendationDto alanResponse = objectMapper.readValue(cleanedAlanJson, AlanRecommendationDto.class);

            if (alanResponse != null && alanResponse.recommendations() != null) {
                return alanResponse.recommendations().stream()
                        .map(item -> {
                            String safeTitle = item.title().replace("<", "&lt;").replace(">", "&gt;");
                            return "<li><a href=\"" + item.url() + "\" target=\"_blank\">" + safeTitle + "</a></li>";
                        })
                        .collect(Collectors.joining("", "<ul>", "</ul>"));
            }
        } catch (Exception e) {
            log.warn("Failed to process recommendation for keyword '{}'", keyword, e);
        }
        return "<ul><li>No recommendation found for this topic.</li></ul>";
    }
}
