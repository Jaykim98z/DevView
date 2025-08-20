package com.allinone.DevView.interview.repository;

import com.allinone.DevView.interview.entity.InterviewResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;      // 추가
import org.springframework.data.domain.Pageable; // 추가

import java.util.List;
import java.util.Optional;

public interface InterviewResultRepository extends JpaRepository<InterviewResult, Long> {
    Optional<InterviewResult> findByInterviewId(Long interviewId);

    /** 사용자별 모든 면접 결과 조회 (최신 생성일 순) */
    @Query("""
        SELECT ir
        FROM InterviewResult ir
        JOIN FETCH ir.interview i
        WHERE i.user.userId = :userId
        ORDER BY i.createdAt DESC
    """)
    List<InterviewResult> findByUserId(@Param("userId") Long userId);

    // ===========================
    // 추가: 최신 1건 자동 조회용
    // ===========================
    Optional<InterviewResult> findTopByInterview_User_UserIdOrderByCreatedAtDesc(Long userId); // 추가

    // ===========================
    // 추가: 페이지네이션 목록 조회용
    //  - 커뮤니티 "결과 선택" 모달에서 사용
    //  - Page/ Pageable 사용으로 안전한 페이징
    // ===========================
    Page<InterviewResult> findByInterview_User_UserId(Long userId, Pageable pageable); // 추가
}
