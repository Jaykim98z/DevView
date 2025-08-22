package com.allinone.DevView.community.service;

import com.allinone.DevView.community.dto.CommentsDto;
import com.allinone.DevView.community.dto.PostListDto;
import com.allinone.DevView.community.dto.CommunityPostsDto;
import com.allinone.DevView.community.entity.Comments;
import com.allinone.DevView.community.entity.CommunityPosts;
import com.allinone.DevView.community.repository.CommentsRepository;
import com.allinone.DevView.community.repository.CommunityPostsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CommunityQueryService {

    private final CommunityPostsRepository postsRepository;
    private final CommentsRepository commentsRepository;
    private final CommunityPostsRepository communityPostsRepository;

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 50;

    private static final Set<String> POST_SORT_WHITELIST = Set.of(
            "createdAt", "likeCount", "scrapCount", "viewCount", "score"
    );
    private static final Set<String> COMMENT_SORT_WHITELIST = Set.of(
            "createdAt"
    );

    private static final Map<String, String> CATEGORY_MAP = Map.ofEntries(
            Map.entry("백엔드", "BACKEND"),
            Map.entry("프론트엔드", "FRONTEND"),
            Map.entry("풀스택", "FULLSTACK"),
            Map.entry("devops", "DEVOPS"),
            Map.entry("devops ", "DEVOPS"),
            Map.entry("data/ai", "DATA_AI"),
            Map.entry("dataai", "DATA_AI"),
            Map.entry("data ai", "DATA_AI")
    );

    private static final Map<String, String> LEVEL_MAP = Map.ofEntries(
            Map.entry("주니어", "JUNIOR"),
            Map.entry("미드레벨", "MID"),
            Map.entry("시니어", "SENIOR")
    );

    @Transactional(readOnly = true)
    public Page<PostListDto> getPosts(Pageable pageable) {
        Pageable safe = sanitizePageable(pageable, "createdAt", Sort.Direction.DESC, POST_SORT_WHITELIST);
        return postsRepository.findAll(safe).map(this::toPostListDto);
    }

    @Transactional(readOnly = true)
    public Page<PostListDto> getPosts(Pageable pageable, String category, String level) {
        Pageable safe = sanitizePageable(pageable, "createdAt", Sort.Direction.DESC, POST_SORT_WHITELIST);

        String c = normalizeFilter(category);
        String l = normalizeFilter(level);

        String mappedCategory = mapCategory(c);
        String mappedLevel = mapLevel(l);

        if (mappedCategory == null && mappedLevel == null) {
            return postsRepository.findAll(safe).map(this::toPostListDto);
        }
        return communityPostsRepository.searchByFilters(mappedCategory, mappedLevel, safe)
                .map(this::toPostListDto);
    }

    @Transactional(readOnly = true)
    public Page<CommentsDto.Res> getComments(Long postId, Pageable pageable) {
        Pageable safe = sanitizePageable(pageable, "createdAt", Sort.Direction.DESC, COMMENT_SORT_WHITELIST);
        return commentsRepository.findByPostIdAndDeletedFalse(postId, safe)
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

    private String normalizeFilter(String v) {
        if (v == null) return null;
        String s = v.trim();
        if (s.isEmpty() || "전체".equals(s)) return null;
        return s;
    }

    private String mapCategory(String v) {
        if (v == null) return null;
        String key = v.toLowerCase().replaceAll("\\s+", "");
        return CATEGORY_MAP.getOrDefault(key, v);
    }

    private String mapLevel(String v) {
        if (v == null) return null;
        return LEVEL_MAP.getOrDefault(v, v);
    }

    private PostListDto toPostListDto(CommunityPosts p) {
        int likeCount  = p.getLikeCount();
        int scrapCount = p.getScrapCount();
        int viewCount  = p.getViewCount();
        int score      = (p.getScore() == null) ? 0 : p.getScore();

        String displayName = (p.getUser() != null && StringUtils.hasText(p.getUser().getUsername()))
                ? p.getUser().getUsername()
                : "익명";

        return new PostListDto(
                p.getPostId(),
                p.getTitle(),
                displayName,
                p.getCategory(),
                p.getGrade(),
                p.getLevel(),
                p.getTechTag(),
                likeCount,
                scrapCount,
                viewCount,
                score,
                p.getCreatedAt()
        );
    }

    private CommentsDto.Res toCommentDto(Comments c) {
        String username = StringUtils.hasText(c.getWriterName()) ? c.getWriterName() : "익명";
        return new CommentsDto.Res(
                c.getId(),
                c.getUserId(),
                username,
                username,
                c.getContent(),
                c.getCreatedAt()
        );
    }
}
