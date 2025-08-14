package com.allinone.DevView.interview.repository;

import com.allinone.DevView.interview.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InterviewRepository extends JpaRepository<Interview, Long> {

    @Query("SELECT i FROM Interview i JOIN FETCH i.questions WHERE i.id = :interviewId")
    Optional<Interview> findByIdWithQuestions(@Param("interviewId") Long interviewId);

    @Query("SELECT i FROM Interview i WHERE i.user.userId = :userId ORDER BY i.createdAt DESC")
    List<Interview> findAllByUserId(@Param("userId") Long userId);
}
