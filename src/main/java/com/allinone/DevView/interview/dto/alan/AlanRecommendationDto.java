package com.allinone.DevView.interview.dto.alan;

import java.util.List;

public record AlanRecommendationDto(List<RecommendationItem> recommendations) {
    public static record RecommendationItem(String title, String url) {}
}
