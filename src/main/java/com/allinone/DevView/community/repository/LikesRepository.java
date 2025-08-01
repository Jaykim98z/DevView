package com.allinone.DevView.community.repository;

import com.allinone.DevView.community.entity.Likes;
import com.allinone.DevView.community.entity.CommunityPosts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikesRepository extends JpaRepository<Likes, Long> {
    Optional<Likes> findByPostIdAndUserId(Long postId, Long userId);
    int countByPostId(Long postId);
}
