package com.allinone.DevView.community.repository;

import com.allinone.DevView.community.entity.CommunityPosts;
import com.allinone.DevView.common.enums.Grade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommunityPostsRepository extends JpaRepository<CommunityPosts, Long> {

    @Query("SELECT p FROM CommunityPosts p JOIN FETCH p.user WHERE p.deleted = false ORDER BY p.createdAt DESC")
    List<CommunityPosts> findAllWithUser();

    @Query("SELECT p FROM CommunityPosts p JOIN FETCH p.user WHERE p.postId = :postId AND p.deleted = false")
    Optional<CommunityPosts> findByIdWithUser(@Param("postId") Long postId);

    List<CommunityPosts> findByCategoryAndLevel(String category, String level);
    List<CommunityPosts> findByCategory(String category);
    List<CommunityPosts> findByGradeOrderByCreatedAtDesc(Grade grade);
    List<CommunityPosts> findByTitleContainingIgnoreCase(String keyword);

    boolean existsByInterviewResultId(Long interviewResultId);

    List<CommunityPosts> findByTypeOrderByCreatedAtDesc(String type);

    @Query("""
           SELECT p
           FROM CommunityPosts p
           JOIN FETCH p.user
           WHERE p.type = 'INTERVIEW_SHARE' AND p.deleted = false
           ORDER BY p.createdAt DESC
           """)
    List<CommunityPosts> findInterviewShareWithUser();

    @Query("""
           SELECT p
           FROM CommunityPosts p
           JOIN FETCH p.user
           WHERE p.postId = :postId AND p.type = 'INTERVIEW_SHARE' AND p.deleted = false
           """)
    Optional<CommunityPosts> findInterviewShareByIdWithUser(@Param("postId") Long postId);

    @Query(
            value = """
                SELECT p
                FROM CommunityPosts p
                JOIN FETCH p.user
                WHERE p.type = 'INTERVIEW_SHARE' AND p.deleted = false
                ORDER BY p.createdAt DESC
                """,
            countQuery = """
                     SELECT COUNT(p)
                     FROM CommunityPosts p
                     WHERE p.type = 'INTERVIEW_SHARE' AND p.deleted = false
                     """
    )
    Page<CommunityPosts> pageInterviewShareWithUser(Pageable pageable);

    List<CommunityPosts> findByTypeAndTitleContainingIgnoreCase(String type, String keyword);
    List<CommunityPosts> findByTypeAndContentContainingIgnoreCase(String type, String keyword);
    List<CommunityPosts> findTop10ByTypeOrderByScoreDesc(String type);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update CommunityPosts p set p.viewCount = p.viewCount + 1 where p.postId = :postId")
    int incrementViewCount(@Param("postId") Long postId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update CommunityPosts p set p.likeCount = p.likeCount + 1 where p.postId = :postId")
    int incrementLikeCount(@Param("postId") Long postId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           update CommunityPosts p
           set p.likeCount = case when p.likeCount > 0 then p.likeCount - 1 else 0 end
           where p.postId = :postId
           """)
    int decrementLikeCount(@Param("postId") Long postId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update CommunityPosts p set p.scrapCount = p.scrapCount + 1 where p.postId = :postId")
    int incrementScrapCount(@Param("postId") Long postId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
       update CommunityPosts p
       set p.scrapCount = case when p.scrapCount > 0 then p.scrapCount - 1 else 0 end
       where p.postId = :postId
       """)
    int decrementScrapCount(@Param("postId") Long postId);

    @EntityGraph(attributePaths = {"user"})
    Page<CommunityPosts> findByDeletedFalse(Pageable pageable);

    Optional<CommunityPosts> findByPostIdAndDeletedFalse(Long postId);

    @Query("select p from CommunityPosts p where p.postId = :postId and p.deleted = false")
    Optional<CommunityPosts> findActiveByPostId(@Param("postId") Long postId);
}
