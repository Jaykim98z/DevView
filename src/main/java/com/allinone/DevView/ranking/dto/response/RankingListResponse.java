// RankingListResponse.java - 랭킹 페이지용 전체 응답 DTO
package com.allinone.DevView.ranking.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 랭킹 페이지 전체 응답 DTO (심플 버전)
 */
@Getter
@Builder
public class RankingListResponse {

    /**
     * 상위 3명 (시상대용)
     */
    private List<RankingResponse> top3Rankings;

    /**
     * 상위 20명 (테이블용) - 실제 사용자 수에 따라 더 적을 수 있음
     */
    private List<RankingResponse> top20Rankings;

    /**
     * 전체 랭킹 참여자 수
     */
    private Long totalUsers;
}