package com.allinone.DevView.community.repository;

import com.allinone.DevView.community.entity.Comments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentsRepository extends JpaRepository<Comments, Long> {

    Page<Comments> findByPostIdAndDeletedFalseOrderByCreatedAtDesc(Long postId, Pageable pageable);
    Slice<Comments> findSliceByPostIdAndDeletedFalseOrderByCreatedAtDesc(Long postId, Pageable pageable);
    List<Comments> findByParentIdAndDeletedFalseOrderByCreatedAtAsc(Long parentId);
    Page<Comments> findByUserIdAndDeletedFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);
    long countByPostIdAndDeletedFalse(Long postId);
    boolean existsByIdAndUserIdAndDeletedFalse(Long id, Long userId);

    @Deprecated
    @Query("SELECT c FROM Comments c WHERE c.postId = :postId AND c.deleted = false ORDER BY c.createdAt DESC")
    Page<Comments> findByPostId(@Param("postId") Long postId, Pageable pageable);

    @Deprecated
    @Query("SELECT c FROM Comments c WHERE c.postId = :postId AND c.deleted = false ORDER BY c.createdAt ASC")
    List<Comments> findByPostIdOrderByCreatedAtAsc(@Param("postId") Long postId);

    @Deprecated
    @Query("SELECT c FROM Comments c WHERE c.userId = :userId AND c.deleted = false ORDER BY c.createdAt DESC")
    List<Comments> findByUserId(@Param("userId") Long userId);

    Page<Comments> findByPostIdAndDeletedFalse(Long postId, Pageable pageable);
}
