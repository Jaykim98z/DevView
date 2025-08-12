package com.allinone.DevView.community.service;

import com.allinone.DevView.community.dto.ToggleResponse;
import com.allinone.DevView.community.entity.Likes;
import com.allinone.DevView.community.entity.Scraps;
import com.allinone.DevView.community.repository.LikesRepository;
import com.allinone.DevView.community.repository.ScrapsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EngagementService {

    private final LikesRepository likesRepository;
    private final ScrapsRepository scrapsRepository;

    @Transactional
    public ToggleResponse toggleLike(Long postId, Long userId) {
        if (likesRepository.existsByUserIdAndPostId(userId, postId)) {
            likesRepository.deleteByUserIdAndPostId(userId, postId);
            long count = likesRepository.countByPostId(postId);
            return new ToggleResponse(false, count);
        } else {
            try {
                Likes like = new Likes();
                like.setUserId(userId);
                like.setPostId(postId);
                likesRepository.save(like);
            } catch (DataIntegrityViolationException ignore) {

            }
            long count = likesRepository.countByPostId(postId);
            return new ToggleResponse(true, count);
        }
    }

    @Transactional
    public ToggleResponse toggleScrap(Long postId, Long userId) {
        if (scrapsRepository.existsByUserIdAndPostId(userId, postId)) {
            scrapsRepository.deleteByUserIdAndPostId(userId, postId);
            long count = scrapsRepository.countByPostId(postId);
            return new ToggleResponse(false, count);
        } else {
            try {
                Scraps scrap = new Scraps();
                scrap.setUserId(userId);
                scrap.setPostId(postId);
                scrapsRepository.save(scrap);
            } catch (DataIntegrityViolationException ignore) {

            }
            long count = scrapsRepository.countByPostId(postId);
            return new ToggleResponse(true, count);
        }
    }
}
