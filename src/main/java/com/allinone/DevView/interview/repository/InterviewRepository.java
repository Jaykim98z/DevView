package com.allinone.DevView.interview.repository;

import com.allinone.DevView.interview.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewRepository extends JpaRepository<Interview, Long> {
}
