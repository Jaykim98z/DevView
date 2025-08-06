package com.allinone.DevView.community.repository;

import com.allinone.DevView.community.entity.Scraps;
import com.allinone.DevView.community.entity.CommunityPosts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScrapsRepository extends JpaRepository<Scraps, Long> {
    Optional<Scraps> findByPostIdAndUserId(Long postId, Long userId);
    int countByPostId(Long postId);
}
