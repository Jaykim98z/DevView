package com.allinone.DevView.community.service;

import com.allinone.DevView.community.dto.CommentsDto;
import com.allinone.DevView.community.dto.PostListDto;
import com.allinone.DevView.community.entity.Comments;
import com.allinone.DevView.community.entity.CommunityPosts;
import com.allinone.DevView.community.repository.CommentsRepository;
import com.allinone.DevView.community.repository.CommunityPostsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CommunityQueryService {

    private final CommunityPostsRepository postsRepository;
    private final CommentsRepository commentsRepository;

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 50;

    private static final Set<String> POST_SORT_WHITELIST = Set.of(
            "createdAt", "likeCount", "scrapCount", "viewCount", "score"
    );
    private static final Set<String> COMMENT_SORT_WHITELIST = Set.of(
            "createdAt"
    );

    /* 게시글 목록 */
    public Page<PostListDto> getPosts(Pageable pageable) {
        Pageable safe = sanitizePageable(pageable, "createdAt", Sort.Direction.DESC, POST_SORT_WHITELIST);
        return postsRepository.findAll(safe)
                .map(this::toPostListDto);
    }

    /* 특정 게시글의 댓글 목록 */
    public Page<CommentsDto> getComments(Long postId, Pageable pageable) {
        Pageable safe = sanitizePageable(pageable, "createdAt", Sort.Direction.DESC, COMMENT_SORT_WHITELIST);
        return commentsRepository.findByPostId(postId, safe)
                .map(this::toCommentDto);
    }

    private Pageable sanitizePageable(Pageable pageable,
                                      String defaultSortProp,
                                      Sort.Direction defaultDir,
                                      Set<String> whitelist) {

        int page = (pageable == null || pageable.getPageNumber() < 0) ? DEFAULT_PAGE : pageable.getPageNumber();
        int size = (pageable == null) ? DEFAULT_SIZE : pageable.getPageSize();
        if (size <= 0) size = DEFAULT_SIZE;
        if (size > MAX_SIZE) {
            throw new IllegalArgumentException("size must be <= " + MAX_SIZE);
        }

        Sort sort = (pageable == null) ? Sort.by(defaultDir, defaultSortProp) : filterSort(pageable.getSort(), whitelist);
        if (sort.isUnsorted()) {
            sort = Sort.by(defaultDir, defaultSortProp);
        }
        return PageRequest.of(page, size, sort);
    }

    private Sort filterSort(Sort input, Set<String> whitelist) {
        List<Sort.Order> orders = new ArrayList<>();
        for (Sort.Order order : input) {
            String prop = order.getProperty();
            if (StringUtils.hasText(prop) && whitelist.contains(prop)) {
                orders.add(order);
            }
        }
        return orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
    }

    private PostListDto toPostListDto(CommunityPosts p) {
        return new PostListDto(
                p.getPostId(),
                p.getTitle(),
                (StringUtils.hasText(p.getWriterName()) ? p.getWriterName()
                        : (p.getUser() != null ? p.getUser().getUsername() : null)),
                p.getCategory(),
                p.getGrade(),
                p.getLevel(),
                p.getTechTag(),
                p.getLikeCount(),
                p.getScrapCount(),
                p.getViewCount(),
                p.getScore(),
                p.getCreatedAt()
        );
    }

    private CommentsDto toCommentDto(Comments c) {
        return new CommentsDto(
                c.getId(),
                c.getPostId(),
                c.getUserId(),
                c.getContent(),
                c.getCreatedAt()
        );
    }
}
