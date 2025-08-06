package com.allinone.DevView.interview.repository;

import com.allinone.DevView.interview.entity.InterviewQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Long> {
}
