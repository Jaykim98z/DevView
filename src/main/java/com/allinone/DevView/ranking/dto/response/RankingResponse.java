// RankingResponse.java - 랭킹 목록용 DTO
package com.allinone.DevView.ranking.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 랭킹 목록 조회 응답 DTO
 */
@Getter
@Builder
public class RankingResponse {

    /**
     * 사용자 ID
     */
    private Long userId;

    /**
     * 사용자명
     */
    private String username;

    /**
     * 프로필 이미지 URL
     */
    private String profileImageUrl;

    /**
     * 랭킹 점수 (평균점수 + 참여횟수×5)
     */
    private Double rankingScore;

    /**
     * 현재 순위
     */
    private Integer currentRank;

    /**
     * 최근 10회 평균 점수
     */
    private Double averageScore;

    /**
     * 총 면접 참여 횟수
     */
    private Integer totalInterviews;
}
