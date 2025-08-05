package com.devview.mypage.repository;

import com.devview.mypage.entity.Scrap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScrapRepository extends JpaRepository<Scrap, Long> {

    List<Scrap> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);
}
