package com.allinone.DevView.ranking.controller;

import com.allinone.DevView.ranking.dto.response.RankingListResponse;
import com.allinone.DevView.ranking.dto.response.RankingResponse;
import com.allinone.DevView.ranking.dto.response.UserRankingResponse;
import com.allinone.DevView.ranking.service.RankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 랭킹 시스템 REST API 컨트롤러
 * JSON 응답만 담당 (페이지 이동은 RankingViewController에서 처리)
 */
@RestController
@RequestMapping("/api/rankings")
@RequiredArgsConstructor
@Slf4j
public class RankingController {

    private final RankingService rankingService;

    /**
     * 랭킹 페이지용 전체 데이터 조회
     * GET /api/rankings
     */
    @GetMapping
    public ResponseEntity<RankingListResponse> getRankingList() {
        log.info("랭킹 페이지 전체 데이터 조회 요청");

        // 상위 3명 (시상대용)
        List<RankingResponse> top3 = rankingService.getTop3Rankings();

        // 상위 20명 (테이블용) - 실제 사용자가 적으면 그만큼만
        List<RankingResponse> top20 = rankingService.getTop20Rankings();

        // 전체 응답 구성 (심플화)
        RankingListResponse response = RankingListResponse.builder()
                .top3Rankings(top3)
                .top20Rankings(top20)
                .totalUsers((long) top20.size())
                .build();

        log.info("랭킹 데이터 조회 완료: top3={}, top20={}", top3.size(), top20.size());
        return ResponseEntity.ok(response);
    }

    /**
     * 상위 20명 랭킹 조회
     * GET /api/rankings/top20
     */
    @GetMapping("/top20")
    public ResponseEntity<List<RankingResponse>> getTop20Rankings() {
        log.info("상위 20명 랭킹 조회 요청");
        List<RankingResponse> rankings = rankingService.getTop20Rankings();
        log.info("상위 20명 랭킹 조회 완료: count={}", rankings.size());
        return ResponseEntity.ok(rankings);
    }

    /**
     * 상위 3명 랭킹 조회 (시상대용)
     * GET /api/rankings/top3
     */
    @GetMapping("/top3")
    public ResponseEntity<List<RankingResponse>> getTop3Rankings() {
        log.info("상위 3명 랭킹 조회 요청");
        List<RankingResponse> rankings = rankingService.getTop3Rankings();
        log.info("상위 3명 랭킹 조회 완료: count={}", rankings.size());
        return ResponseEntity.ok(rankings);
    }

    /**
     * 특정 사용자의 랭킹 정보 조회
     * GET /api/rankings/users/{userId}
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserRankingResponse> getUserRanking(@PathVariable Long userId) {
        log.info("사용자 랭킹 정보 조회 요청: userId={}", userId);
        UserRankingResponse userRanking = rankingService.getUserRanking(userId);
        log.info("사용자 랭킹 정보 조회 완료: userId={}, rank={}", userId, userRanking.getCurrentRank());
        return ResponseEntity.ok(userRanking);
    }

    /**
     * 특정 사용자의 랭킹 정보 업데이트 (면접 완료 후 호출)
     * POST /api/rankings/users/{userId}/update
     */
    @PostMapping("/users/{userId}/update")
    public ResponseEntity<Void> updateUserRanking(@PathVariable Long userId) {
        log.info("사용자 랭킹 업데이트 요청: userId={}", userId);
        rankingService.updateUserRanking(userId);
        log.info("사용자 랭킹 업데이트 완료: userId={}", userId);
        return ResponseEntity.ok().build();
    }

    /**
     * 전체 랭킹 업데이트 (관리자용/스케줄러용)
     * POST /api/rankings/update-all
     */
    @PostMapping("/update-all")
    public ResponseEntity<Void> updateAllRankings() {
        log.info("전체 랭킹 업데이트 요청");
        rankingService.updateAllRankings();
        log.info("전체 랭킹 업데이트 완료");
        return ResponseEntity.ok().build();
    }

    /**
     * 랭킹 시스템 상태 확인 (헬스체크용)
     * GET /api/rankings/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        List<RankingResponse> top3 = rankingService.getTop3Rankings();
        return ResponseEntity.ok("Ranking system is healthy. Top rankings count: " + top3.size());
    }
}