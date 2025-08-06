package com.allinone.DevView.mypage.repository;

import com.allinone.DevView.mypage.entity.Scrap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScrapRepository extends JpaRepository<Scrap, Long> {

    // UserId로 스크랩 데이터를 찾는 메서드
    List<Scrap> findByUserId(Long userId);

    // UserId로 가장 최근 5개의 스크랩을 가져오는 메서드
    List<Scrap> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);
}
