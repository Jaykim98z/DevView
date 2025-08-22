package com.allinone.DevView.interview.repository;

import com.allinone.DevView.interview.entity.InterviewResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    Optional<InterviewResult> findTopByInterview_User_UserIdOrderByInterview_CreatedAtDesc(Long userId);

    Optional<InterviewResult> findByIdAndInterview_User_UserId(Long resultId, Long userId);

    boolean existsByIdAndInterview_User_UserId(Long id, Long userId);

}
