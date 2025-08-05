package com.devview.interview.repository;

import com.devview.mypage.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterviewRepository extends JpaRepository<Interview, Long> {

    List<Interview> findAllByUserId(Long userId);
}
