package com.allinone.DevView.interview.controller;

import com.allinone.DevView.interview.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class RecommendationController {
    private final RecommendationService recommendationService;

    @GetMapping
    public ResponseEntity<String> getRecommendations(@RequestParam String keyword) {
        String recommendations = recommendationService.getRecommendations(keyword);

        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/single")
    public ResponseEntity<String> getSingleRecommendation(@RequestParam String keyword) {
        String recommendationHtml = recommendationService.getSingleRecommendationHtml(keyword);
        return ResponseEntity.ok(recommendationHtml);
    }
}
