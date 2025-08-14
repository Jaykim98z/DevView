package com.allinone.DevView.interview.repository;

import com.allinone.DevView.interview.entity.InterviewAnswer;
import com.allinone.DevView.interview.entity.InterviewQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterviewAnswerRepository extends JpaRepository<InterviewAnswer, Long> {
    List<InterviewAnswer> findByQuestionIn(List<InterviewQuestion> questions);
}
