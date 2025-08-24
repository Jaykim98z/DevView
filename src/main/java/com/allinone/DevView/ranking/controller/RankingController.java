package com.allinone.DevView.ranking.controller;

import com.allinone.DevView.ranking.dto.response.RankingListResponse;
import com.allinone.DevView.ranking.dto.response.RankingResponse;
import com.allinone.DevView.ranking.dto.response.UserRankingResponse;
import com.allinone.DevView.ranking.service.RankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Ranking API", description = "사용자 랭킹 조회 및 업데이트 관련 API")
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
    @Operation(summary = "전체 랭킹 데이터 조회", description = "랭킹 페이지에 필요한 전체 데이터(Top 3, Top 20 등)를 조회합니다.")
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
    @Operation(summary = "상위 20명 랭킹 조회", description = "상위 20명의 랭킹 목록을 조회합니다.")
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
    @Operation(summary = "상위 3명 랭킹 조회", description = "시상대에 표시될 상위 3명의 랭킹 정보를 조회합니다.")
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
    @Operation(summary = "특정 사용자 랭킹 조회", description = "사용자 ID로 해당 사용자의 현재 랭킹 및 점수 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자 또는 랭킹 정보")
    })
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserRankingResponse> getUserRanking(
            @Parameter(description = "랭킹을 조회할 사용자의 ID") @PathVariable Long userId) {
        log.info("사용자 랭킹 정보 조회 요청: userId={}", userId);
        UserRankingResponse userRanking = rankingService.getUserRanking(userId);
        log.info("사용자 랭킹 정보 조회 완료: userId={}, rank={}", userId, userRanking.getCurrentRank());
        return ResponseEntity.ok(userRanking);
    }

    /**
     * 특정 사용자의 랭킹 정보 업데이트 (면접 완료 후 호출)
     * POST /api/rankings/users/{userId}/update
     */
    @Operation(summary = "사용자 랭킹 업데이트", description = "면접 완료 후 특정 사용자의 랭킹 정보를 갱신합니다.")
    @PostMapping("/users/{userId}/update")
    public ResponseEntity<Void> updateUserRanking(
            @Parameter(description = "랭킹을 업데이트할 사용자의 ID") @PathVariable Long userId) {
        log.info("사용자 랭킹 업데이트 요청: userId={}", userId);
        rankingService.updateUserRanking(userId);
        log.info("사용자 랭킹 업데이트 완료: userId={}", userId);
        return ResponseEntity.ok().build();
    }

    /**
     * 전체 랭킹 업데이트 (관리자용/스케줄러용)
     * POST /api/rankings/update-all
     */
    @Operation(summary = "전체 랭킹 업데이트 (관리자용)", description = "모든 사용자의 랭킹을 재계산하고 갱신합니다.")
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
    @Operation(summary = "랭킹 시스템 상태 확인", description = "랭킹 시스템의 정상 동작 여부를 확인하는 헬스체크 엔드포인트입니다.")
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        List<RankingResponse> top3 = rankingService.getTop3Rankings();
        return ResponseEntity.ok("Ranking system is healthy. Top rankings count: " + top3.size());
    }
}