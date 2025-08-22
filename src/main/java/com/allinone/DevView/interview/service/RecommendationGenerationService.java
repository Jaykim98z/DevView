package com.allinone.DevView.interview.service;

import com.allinone.DevView.common.exception.CustomException;
import com.allinone.DevView.common.exception.ErrorCode;
import com.allinone.DevView.interview.dto.alan.AlanRecommendationDto;
import com.allinone.DevView.interview.entity.InterviewResult;
import com.allinone.DevView.interview.repository.InterviewResultRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationGenerationService {
    private final AlanApiService alan;
    private final InterviewResultRepository interviewResultRepository;
    private final ObjectMapper objectMapper;

    @Async
    public void generateAndSaveRecommendations(Long resultId, List<String> keywords) {
        log.info("Starting async recommendation generation for resultId: {}", resultId);
        try {
            String recommendationsHtml = getRecommendationsFromAlan(keywords);

            InterviewResult result = interviewResultRepository.findById(resultId)
                    .orElseThrow(() -> new CustomException(ErrorCode.INTERVIEW_NOT_FOUND));

            result.setRecommendedResource(recommendationsHtml);
            interviewResultRepository.save(result);
            log.info("Finished async recommendation generation for resultId: {}", resultId);
        } catch (Exception e) {
            log.error("Error during async recommendation generation for resultId: {}", resultId, e);
        }
    }

    private String getRecommendationsFromAlan(List<String> keywords) {
        if (alan instanceof AlanApiService && keywords != null && !keywords.isEmpty()) {
            try {
                String combinedKeywords = String.join(", ", keywords);
                String alanJson = ((AlanApiService) alan).getRecommendations(combinedKeywords);
                String cleanedAlanJson = alanJson.trim().replace("```json", "").replace("```", "").trim();

                AlanRecommendationDto alanResponse = objectMapper.readValue(cleanedAlanJson, AlanRecommendationDto.class);

                if (alanResponse != null && alanResponse.recommendations() != null) {
                    return alanResponse.recommendations().stream()
                            .map(item -> {
                                String safeTitle = item.title().replace("<", "&lt;").replace(">", "&gt;");
                                return "<li><a href=\"" + item.url() + "\" target=\"_blank\" rel=\"noopener noreferrer\">" + safeTitle + "</a></li>";
                            })
                            .collect(Collectors.joining("", "<ul>", "</ul>"));
                }
            } catch (Exception e) {
                log.error("Failed to get or process recommendations from Alan API", e);
            }
        }
        return "추천 학습 자료가 없습니다.";
    }
}
