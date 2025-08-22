package com.allinone.DevView.ranking.service;

import com.allinone.DevView.interview.entity.InterviewResult;
import com.allinone.DevView.interview.repository.InterviewResultRepository;
import com.allinone.DevView.ranking.dto.response.RankingResponse;
import com.allinone.DevView.ranking.dto.response.UserRankingResponse;
import com.allinone.DevView.ranking.entity.UserRanking;
import com.allinone.DevView.ranking.repository.UserRankingRepository;
import com.allinone.DevView.user.entity.User;
import com.allinone.DevView.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 랭킹 시스템 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RankingService {

    private final UserRankingRepository userRankingRepository;
    private final InterviewResultRepository interviewResultRepository;
    private final UserRepository userRepository;

    /**
     * 상위 20명 랭킹 조회
     */
    @Transactional(readOnly = true)
    public List<RankingResponse> getTop20Rankings() {
        log.info("상위 20명 랭킹 조회 시작");

        List<UserRanking> rankings = userRankingRepository.findTop20Rankings();

        return rankings.stream()
                .map(this::convertToRankingResponse)
                .collect(Collectors.toList());
    }

    /**
     * 상위 3명 랭킹 조회 (시상대용)
     */
    @Transactional(readOnly = true)
    public List<RankingResponse> getTop3Rankings() {
        log.info("상위 3명 랭킹 조회 시작");

        List<UserRanking> rankings = userRankingRepository.findTop3Rankings();

        return rankings.stream()
                .map(this::convertToRankingResponse)
                .collect(Collectors.toList());
    }

    /**
     * 특정 사용자의 랭킹 정보 조회
     */
    @Transactional(readOnly = true)
    public UserRankingResponse getUserRanking(Long userId) {
        log.info("사용자 랭킹 정보 조회: userId={}", userId);

        Optional<UserRanking> rankingOpt = userRankingRepository.findByUserUserId(userId);

        if (rankingOpt.isEmpty()) {
            // 랭킹 정보가 없으면 새로 계산해서 생성
            return createNewUserRanking(userId);
        }

        UserRanking ranking = rankingOpt.get();

        // 실시간 순위 계산
        Long usersAbove = userRankingRepository.countUsersAbove(
                ranking.getRankingScore(),
                ranking.getTotalInterviews(),
                ranking.getAverageScore()
        );
        Integer currentRank = usersAbove.intValue() + 1;

        Long totalUsers = userRankingRepository.countTotalRankedUsers();

        return UserRankingResponse.builder()
                .userId(userId)
                .username(ranking.getUser().getUsername())
                .averageScore(ranking.getAverageScore())
                .totalInterviews(ranking.getTotalInterviews())
                .rankingScore(ranking.getRankingScore())
                .currentRank(currentRank)
                .totalUsers(totalUsers)
                .build();
    }

    /**
     * 사용자 랭킹 정보 업데이트 (면접 완료 후 호출)
     */
    @Transactional
    public void updateUserRanking(Long userId) {
        log.info("사용자 랭킹 업데이트 시작: userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        // 사용자의 면접 결과 조회 및 통계 계산
        RankingStats stats = calculateUserStats(userId);

        Optional<UserRanking> existingRanking = userRankingRepository.findByUserUserId(userId);

        if (existingRanking.isPresent()) {
            // 기존 랭킹 업데이트
            UserRanking ranking = existingRanking.get();
            ranking.updateRankingInfo(stats.averageScore, stats.totalInterviews);
            userRankingRepository.save(ranking);
            log.info("기존 랭킹 업데이트 완료: userId={}, score={}", userId, ranking.getRankingScore());
        } else {
            // 새로운 랭킹 생성
            UserRanking newRanking = UserRanking.createNewRanking(user, stats.averageScore, stats.totalInterviews);
            userRankingRepository.save(newRanking);
            log.info("새로운 랭킹 생성 완료: userId={}, score={}", userId, newRanking.getRankingScore());
        }
    }

    /**
     * 전체 랭킹 업데이트 (스케줄러용)
     */
    @Transactional
    public void updateAllRankings() {
        log.info("전체 랭킹 업데이트 시작");

        List<User> allUsers = userRepository.findAll();

        for (User user : allUsers) {
            try {
                updateUserRanking(user.getUserId());
            } catch (Exception e) {
                log.error("사용자 랭킹 업데이트 실패: userId={}", user.getUserId(), e);
            }
        }

        log.info("전체 랭킹 업데이트 완료");
    }

    // ========================================
    // 비공개 헬퍼 메서드들
    // ========================================

    /**
     * 사용자의 면접 통계 계산
     */
    private RankingStats calculateUserStats(Long userId) {
        // 사용자의 모든 면접 결과 조회
        List<InterviewResult> allResults = interviewResultRepository.findByUserId(userId);

        if (allResults.isEmpty()) {
            return new RankingStats(0.0, 0);
        }

        // 최근 10회 면접 결과만 추출 (최신순으로 정렬 후)
        List<InterviewResult> recent10Results = allResults.stream()
                .sorted((a, b) -> b.getInterview().getCreatedAt().compareTo(a.getInterview().getCreatedAt()))
                .limit(10)
                .collect(Collectors.toList());

        // 평균 점수 계산
        double averageScore = recent10Results.stream()
                .mapToInt(InterviewResult::getTotalScore)
                .average()
                .orElse(0.0);

        return new RankingStats(averageScore, allResults.size());
    }

    /**
     * UserRanking을 RankingResponse로 변환
     */
    private RankingResponse convertToRankingResponse(UserRanking ranking) {
        // 실시간 순위 계산
        Long usersAbove = userRankingRepository.countUsersAbove(
                ranking.getRankingScore(),
                ranking.getTotalInterviews(),
                ranking.getAverageScore()
        );
        Integer currentRank = usersAbove.intValue() + 1;

        return RankingResponse.builder()
                .userId(ranking.getUser().getUserId())
                .username(ranking.getUser().getUsername())
                .profileImageUrl("/img/profile-default.svg") // TODO: UserProfiles 연동시 실제 이미지로 변경
                .rankingScore(ranking.getRankingScore())
                .currentRank(currentRank)
                .averageScore(ranking.getAverageScore())
                .totalInterviews(ranking.getTotalInterviews())
                .build();
    }

    /**
     * 새로운 사용자 랭킹 생성
     */
    private UserRankingResponse createNewUserRanking(Long userId) {
        log.info("새로운 사용자 랭킹 생성: userId={}", userId);

        // 면접 기록이 없는 사용자
        return UserRankingResponse.builder()
                .userId(userId)
                .username("Unknown")
                .averageScore(0.0)
                .totalInterviews(0)
                .rankingScore(0.0)
                .currentRank(0)
                .totalUsers(userRankingRepository.countTotalRankedUsers())
                .build();
    }

    /**
     * 면접 통계 데이터 클래스
     */
    private static class RankingStats {
        final double averageScore;
        final int totalInterviews;

        RankingStats(double averageScore, int totalInterviews) {
            this.averageScore = averageScore;
            this.totalInterviews = totalInterviews;
        }
    }
}