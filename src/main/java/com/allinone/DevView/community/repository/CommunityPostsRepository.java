package com.allinone.DevView.community.repository;

import com.allinone.DevView.community.entity.CommunityPosts;
import com.allinone.DevView.common.enums.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommunityPostsRepository extends JpaRepository<CommunityPosts, Long> {

    // 사용자까지 Fetch Join으로 가져옴
    @Query("SELECT p FROM CommunityPosts p JOIN FETCH p.user")
    List<CommunityPosts> findAllWithUser();

    // 게시글 하나 상세 조회용
    @Query("SELECT p FROM CommunityPosts p JOIN FETCH p.user WHERE p.postId = :postId")
    Optional<CommunityPosts> findByIdWithUser(Long postId);

    List<CommunityPosts> findByCategory(String category);

    List<CommunityPosts> findByGradeOrderByCreatedAtDesc(Grade grade);

    List<CommunityPosts> findByTitleContainingIgnoreCase(String keyword);

    List<CommunityPosts> findByCategoryAndLevel(String category, String level);
}
