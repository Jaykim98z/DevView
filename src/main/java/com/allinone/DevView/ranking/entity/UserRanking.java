package com.allinone.DevView.ranking.entity;

import com.allinone.DevView.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 사용자 랭킹 정보 엔티티
 */
@Entity
@Table(name = "user_rankings")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRanking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ranking_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /**
     * 최근 10회 면접의 평균 점수
     */
    @Column(nullable = false, columnDefinition = "DECIMAL(5,2) DEFAULT 0.0")
    private Double averageScore;

    /**
     * 총 면접 참여 횟수
     */
    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer totalInterviews;

    /**
     * 계산된 랭킹 점수 (평균점수 + 참여횟수×5)
     */
    @Column(nullable = false, columnDefinition = "DECIMAL(7,2) DEFAULT 0.0")
    private Double rankingScore;

    /**
     * 현재 순위
     */
    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer currentRank;

    /**
     * 마지막 랭킹 업데이트 시간
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ========================================
    // 비즈니스 메서드
    // ========================================

    /**
     * 랭킹 점수 계산 및 업데이트
     * 공식: 최근 10회 평균 점수 + (총 면접 횟수 × 5)
     */
    public void calculateRankingScore() {
        this.rankingScore = this.averageScore + (this.totalInterviews * 5.0);
    }

    /**
     * 랭킹 정보 업데이트
     */
    public void updateRankingInfo(Double averageScore, Integer totalInterviews) {
        this.averageScore = averageScore;
        this.totalInterviews = totalInterviews;
        calculateRankingScore();
    }

    /**
     * 순위 업데이트
     */
    public void updateRank(Integer newRank) {
        this.currentRank = newRank;
    }

    /**
     * 정적 팩토리 메서드 - 새로운 랭킹 생성
     */
    public static UserRanking createNewRanking(User user, Double averageScore, Integer totalInterviews) {
        UserRanking ranking = UserRanking.builder()
                .user(user)
                .averageScore(averageScore)
                .totalInterviews(totalInterviews)
                .currentRank(0) // 초기값
                .build();

        ranking.calculateRankingScore();
        return ranking;
    }
}