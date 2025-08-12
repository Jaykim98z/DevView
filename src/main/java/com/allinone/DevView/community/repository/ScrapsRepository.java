package com.allinone.DevView.community.repository;

import com.allinone.DevView.community.entity.Scraps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScrapsRepository extends JpaRepository<Scraps, Long> {
    List<Scraps> findByUserId(Long userId);

    List<Scraps> findByPostId(Long postId);

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    long countByPostId(Long postId);
    void deleteByUserIdAndPostId(Long userId, Long postId);
}
