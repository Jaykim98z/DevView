package com.allinone.DevView.ranking.repository;

import com.allinone.DevView.ranking.entity.UserRanking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 랭킹 데이터 접근 계층
 */
@Repository
public interface UserRankingRepository extends JpaRepository<UserRanking, Long> {

    /**
     * 사용자 ID로 랭킹 정보 조회 (수정됨)
     */
    Optional<UserRanking> findByUserUserId(Long userId);

    /**
     * 랭킹 점수 기준 상위 N명 조회 (순위 순)
     */
    @Query("SELECT ur FROM UserRanking ur " +
            "JOIN FETCH ur.user u " +
            "ORDER BY ur.rankingScore DESC, ur.totalInterviews DESC, ur.averageScore DESC")
    List<UserRanking> findTopRankings(Pageable pageable);

    /**
     * 랭킹 점수 기준 상위 20명 조회
     */
    @Query("SELECT ur FROM UserRanking ur " +
            "JOIN FETCH ur.user u " +
            "ORDER BY ur.rankingScore DESC, ur.totalInterviews DESC, ur.averageScore DESC " +
            "LIMIT 20")
    List<UserRanking> findTop20Rankings();

    /**
     * 특정 사용자보다 랭킹 점수가 높은 사용자 수 조회 (실시간 순위 계산)
     */
    @Query("SELECT COUNT(ur) FROM UserRanking ur " +
            "WHERE ur.rankingScore > :rankingScore " +
            "OR (ur.rankingScore = :rankingScore AND ur.totalInterviews > :totalInterviews) " +
            "OR (ur.rankingScore = :rankingScore AND ur.totalInterviews = :totalInterviews AND ur.averageScore > :averageScore)")
    Long countUsersAbove(@Param("rankingScore") Double rankingScore,
                         @Param("totalInterviews") Integer totalInterviews,
                         @Param("averageScore") Double averageScore);

    /**
     * 전체 랭킹 사용자 수 조회
     */
    @Query("SELECT COUNT(ur) FROM UserRanking ur WHERE ur.totalInterviews > 0")
    Long countTotalRankedUsers();

    /**
     * 랭킹 점수 기준 전체 조회 (페이징)
     */
    @Query("SELECT ur FROM UserRanking ur " +
            "JOIN FETCH ur.user u " +
            "WHERE ur.totalInterviews > 0 " +
            "ORDER BY ur.rankingScore DESC, ur.totalInterviews DESC, ur.averageScore DESC")
    Page<UserRanking> findAllRankings(Pageable pageable);

    /**
     * 면접 참여 이력이 있는 사용자들의 랭킹만 조회
     */
    @Query("SELECT ur FROM UserRanking ur " +
            "JOIN FETCH ur.user u " +
            "WHERE ur.totalInterviews > 0 " +
            "ORDER BY ur.rankingScore DESC")
    List<UserRanking> findActiveUserRankings();

    /**
     * 사용자 존재 여부 확인 (수정됨)
     */
    boolean existsByUserUserId(Long userId);

    /**
     * 특정 사용자의 순위 업데이트 (배치 처리용) (수정됨)
     */
    @Modifying
    @Query("UPDATE UserRanking ur SET ur.currentRank = :newRank WHERE ur.user.userId = :userId")
    void updateUserRank(@Param("userId") Long userId, @Param("newRank") Integer newRank);

    /**
     * 상위 3명의 랭킹 조회 (시상대용)
     */
    @Query("SELECT ur FROM UserRanking ur " +
            "JOIN FETCH ur.user u " +
            "WHERE ur.totalInterviews > 0 " +
            "ORDER BY ur.rankingScore DESC, ur.totalInterviews DESC, ur.averageScore DESC " +
            "LIMIT 3")
    List<UserRanking> findTop3Rankings();
}