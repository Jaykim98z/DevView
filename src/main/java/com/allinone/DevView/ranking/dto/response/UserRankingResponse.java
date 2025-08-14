// UserRankingResponse.java - 개인 랭킹용 DTO
package com.allinone.DevView.ranking.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 개인 랭킹 정보 조회 응답 DTO (심플 버전)
 */
@Getter
@Builder
public class UserRankingResponse {

    /**
     * 사용자 ID
     */
    private Long userId;

    /**
     * 사용자명
     */
    private String username;

    /**
     * 최근 10회 평균 점수
     */
    private Double averageScore;

    /**
     * 총 면접 참여 횟수
     */
    private Integer totalInterviews;

    /**
     * 랭킹 점수
     */
    private Double rankingScore;

    /**
     * 현재 순위
     */
    private Integer currentRank;

    /**
     * 전체 랭킹 참여자 수
     */
    private Long totalUsers;
}