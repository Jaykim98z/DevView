package com.allinone.DevView.community.repository;

import com.allinone.DevView.community.entity.CommunityPosts;
import com.allinone.DevView.common.enums.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommunityPostsRepository extends JpaRepository<CommunityPosts, Long> {
    List<CommunityPosts> findByCategory(String category);

    List<CommunityPosts> findByGradeOrderByCreatedAtDesc(Grade grade);

    List<CommunityPosts> findByTitleContainingIgnoreCase(String keyword);

    List<CommunityPosts> findByCategoryAndLevel(String category, String level);
}

