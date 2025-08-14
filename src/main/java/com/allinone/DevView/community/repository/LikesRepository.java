package com.allinone.DevView.community.repository;

import com.allinone.DevView.community.entity.Likes;
import com.allinone.DevView.community.entity.LikesId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LikesRepository extends JpaRepository<Likes, LikesId> {

    List<Likes> findByPostId(Long postId);

    List<Likes> findByUserId(Long userId);

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    long countByPostId(Long postId);
    void deleteByUserIdAndPostId(Long userId, Long postId);
}
