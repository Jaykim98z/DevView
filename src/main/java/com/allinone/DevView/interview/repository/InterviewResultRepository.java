package com.allinone.DevView.interview.repository;

import com.allinone.DevView.interview.entity.InterviewResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InterviewResultRepository extends JpaRepository<InterviewResult, Long> {
    Optional<InterviewResult> findByInterviewId(Long interviewId);
}
