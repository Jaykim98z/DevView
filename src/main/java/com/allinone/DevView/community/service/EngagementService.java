package com.allinone.DevView.community.service;

import com.allinone.DevView.community.dto.ToggleResponse;
import com.allinone.DevView.community.entity.Likes;
import com.allinone.DevView.community.entity.LikesId;
import com.allinone.DevView.community.entity.Scraps;
import com.allinone.DevView.community.repository.CommunityPostsRepository;
import com.allinone.DevView.community.repository.LikesRepository;
import com.allinone.DevView.community.repository.ScrapsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EngagementService {

    private final LikesRepository likesRepository;
    private final ScrapsRepository scrapsRepository;
    private final CommunityPostsRepository postsRepository;

    @Transactional
    public ToggleResponse toggleLike(Long postId, Long userId) {
        boolean existed = likesRepository.existsByUserIdAndPostId(userId, postId);

        if (existed) {
            // 좋아요 취소
            LikesId id = new LikesId();
            id.setUserId(userId);
            id.setPostId(postId);
            likesRepository.deleteById(id);

            postsRepository.decrementLikeCount(postId);
            long current = likesRepository.countByPostId(postId);
            return new ToggleResponse(false, current);
        } else {

            Likes like = new Likes();
            like.setUserId(userId);
            like.setPostId(postId);
            like.setCreatedAt(LocalDateTime.now());
            likesRepository.save(like);

            postsRepository.incrementLikeCount(postId);
            long current = likesRepository.countByPostId(postId);
            return new ToggleResponse(true, current);
        }
    }

    @Transactional
    public ToggleResponse toggleScrap(Long postId, Long userId) {
        boolean existed = scrapsRepository.existsByUserIdAndPostId(userId, postId);

        if (existed) {

            scrapsRepository.deleteByUserIdAndPostId(userId, postId);
            postsRepository.decrementScrapCount(postId);
            long current = scrapsRepository.countByPostId(postId);
            return new ToggleResponse(false, current);
        } else {

            Scraps s = new Scraps();
            s.setUserId(userId);
            s.setPostId(postId);

            try {
                var createdAtField = Scraps.class.getDeclaredField("createdAt");
                createdAtField.setAccessible(true);
                if (createdAtField.getType().equals(LocalDateTime.class)) {
                    s.setCreatedAt(LocalDateTime.now());
                }
            } catch (NoSuchFieldException ignored) { }

            scrapsRepository.save(s);

            postsRepository.incrementScrapCount(postId);
            long current = scrapsRepository.countByPostId(postId);
            return new ToggleResponse(true, current);
        }
    }
}
