package com.allinone.DevView.interview.repository;

import com.allinone.DevView.interview.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterviewRepository extends JpaRepository<Interview, Long> {

    // UserId로 면접 데이터를 찾는 메서드
    List<Interview> findAllByUserId(Long userId);

    // 면접 일자 기준으로 내림차순으로 정렬
    List<Interview> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
