package com.allinone.DevView.mypage.repository.;

import com.allinone.DevView.mypage.entity.Scrap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScrapRepository extends JpaRepository<Scrap, Long> {

    List<Scrap> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);
}
