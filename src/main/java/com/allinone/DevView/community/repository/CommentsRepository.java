package com.allinone.DevView.community.repository;

import com.allinone.DevView.community.entity.Comments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentsRepository extends JpaRepository<Comments, Long> {

    List<Comments> findByPostIdOrderByCreatedAtAsc(Long postId);

    List<Comments> findByUserId(Long userId);

}
