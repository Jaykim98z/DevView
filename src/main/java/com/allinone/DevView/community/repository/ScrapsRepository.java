package com.allinone.DevView.community.repository;

import com.allinone.DevView.community.entity.Scraps;
import com.allinone.DevView.mypage.dto.ScrapDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ScrapsRepository extends JpaRepository<Scraps, Long> {

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    long countByPostId(Long postId);

    List<Scraps> findByUserId(Long userId);

    Optional<Scraps> findByUserIdAndPostId(Long userId, Long postId);

    int deleteByUserIdAndPostId(Long userId, Long postId);

    // 마이페이지용: 제목/링크/좋아요/댓글수 + 작성자/요약
    @Query("""
                        select new com.allinone.DevView.mypage.dto.ScrapDto(
                p.postId,
                p.title,
                concat('/community/', p.postId),
                p.likeCount,
                (select count(c) from Comments c where c.postId = p.postId and c.deleted = false),
                p.user.username,
                case when length(p.content) <= 70 then p.content else concat(substring(p.content, 1, 70), '...')
                end)
            from Scraps s, CommunityPosts p
            where p.postId = s.postId
              and s.userId = :userId
            order by s.createdAt desc
            """)
    List<ScrapDto> findMypageScrapListByUserId(@Param("userId") Long userId);
}
