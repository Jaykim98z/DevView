package com.allinone.DevView.community.repository;

import com.allinone.DevView.community.entity.Scraps;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScrapsRepository extends JpaRepository<Scraps, Long> {

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    long countByPostId(Long postId);

    List<Scraps> findByUserId(Long userId);

    Optional<Scraps> findByUserIdAndPostId(Long userId, Long postId);

    int deleteByUserIdAndPostId(Long userId, Long postId);
}
