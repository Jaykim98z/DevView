package com.allinone.DevView.community.service;

import com.allinone.DevView.community.dto.CommentsDto;
import com.allinone.DevView.community.dto.PostListDto;
import com.allinone.DevView.community.entity.Comments;
import com.allinone.DevView.community.entity.CommunityPosts;
import com.allinone.DevView.community.repository.CommentsRepository;
import com.allinone.DevView.community.repository.CommunityPostsRepository;
import com.allinone.DevView.common.enums.JobPosition;
import com.allinone.DevView.common.enums.CareerLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Transactional(readOnly = true)
    public Page<PostListDto> getPosts(Pageable pageable) {
        Pageable safe = sanitizePageable(pageable, "createdAt", Sort.Direction.DESC, POST_SORT_WHITELIST);
        return postsRepository.findAllByDeletedFalse(safe).map(this::toPostListDto);
    }

    @Transactional(readOnly = true)
    public Page<PostListDto> getPosts(Pageable pageable, String category, String level) {
        JobPosition catEnum = toJobPosition(category);
        CareerLevel lvlEnum = toCareerLevel(level);
        return getPosts(pageable, catEnum, lvlEnum);
    }

    @Transactional(readOnly = true)
    public Page<PostListDto> getPosts(Pageable pageable, JobPosition category, CareerLevel level) {
        Pageable safe = sanitizePageable(pageable, "createdAt", Sort.Direction.DESC, POST_SORT_WHITELIST);
        Specification<CommunityPosts> spec = (root, q, cb) -> cb.isFalse(root.get("deleted"));

        if (category != null) {
            String cat = category.name();
            spec = spec.and((r, q2, cb2) ->
                    cb2.equal(cb2.upper(r.get("category")), cat)
            );
        }

        if (level != null) {
            String lvl = level.name();
            spec = spec.and((r, q2, cb2) ->
                    cb2.equal(cb2.upper(r.get("level")), lvl)
            );
        }

        return postsRepository.findAll(spec, safe).map(this::toPostListDto);
    }

    @Transactional(readOnly = true)
    public Page<PostListDto> getPostsByJob(Pageable pageable, String job, String category, String level, String keyword) {
        Pageable safe = sanitizePageable(pageable, "createdAt", Sort.Direction.DESC, POST_SORT_WHITELIST);

        JobMap mapped = mapJob(job);
        String cat = firstNonBlank(category, mapped.category());
        String lvl = firstNonBlank(level, mapped.level());

        if (StringUtils.hasText(cat)) cat = cat.trim().toUpperCase();
        if (StringUtils.hasText(lvl)) lvl = lvl.trim().toUpperCase();

        Specification<CommunityPosts> spec = (root, q, cb) -> cb.isFalse(root.get("deleted"));

        if (StringUtils.hasText(cat) && !"전체".equals(cat)) {
            String catFinal = cat;
            spec = spec.and((r, q2, cb2) -> cb2.equal(cb2.upper(r.get("category")), catFinal));
        }
        if (StringUtils.hasText(lvl) && !"전체".equals(lvl)) {
            String lvlFinal = lvl;
            spec = spec.and((r, q2, cb2) -> cb2.equal(cb2.upper(r.get("level")), lvlFinal));
        }
        if (StringUtils.hasText(keyword)) {
            String like = "%" + keyword.trim().toLowerCase() + "%";
            spec = spec.and((r, q2, cb2) ->
                    cb2.or(
                            cb2.like(cb2.lower(r.get("title")), like),
                            cb2.like(cb2.lower(r.get("content")), like)
                    )
            );
        }

        return postsRepository.findAll(spec, safe).map(this::toPostListDto);
    }

    @Transactional(readOnly = true)
    public Page<CommentsDto.Res> getComments(Long postId, Pageable pageable) {
        Pageable safe = sanitizePageable(pageable, "createdAt", Sort.Direction.DESC, COMMENT_SORT_WHITELIST);
        return commentsRepository.findByPostIdAndDeletedFalse(postId, safe).map(this::toCommentDto);
    }

    private Pageable sanitizePageable(Pageable pageable,
                                      String defaultSortProp,
                                      Sort.Direction defaultDir,
                                      Set<String> whitelist) {
        int page = (pageable == null || pageable.getPageNumber() < 0) ? DEFAULT_PAGE : pageable.getPageNumber();
        int size = (pageable == null) ? DEFAULT_SIZE : pageable.getPageSize();
        if (size <= 0) size = DEFAULT_SIZE;
        if (size > MAX_SIZE) throw new IllegalArgumentException("size must be <= " + MAX_SIZE);
        Sort sort = (pageable == null) ? Sort.by(defaultDir, defaultSortProp) : filterSort(pageable.getSort(), whitelist);
        if (sort.isUnsorted()) sort = Sort.by(defaultDir, defaultSortProp);
        return PageRequest.of(page, size, sort);
    }

    private Sort filterSort(Sort input, Set<String> whitelist) {
        List<Sort.Order> orders = new ArrayList<>();
        for (Sort.Order order : input) {
            String prop = order.getProperty();
            if (StringUtils.hasText(prop) && whitelist.contains(prop)) orders.add(order);
        }
        return orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
    }

    private PostListDto toPostListDto(CommunityPosts p) {
        int likeCount = p.getLikeCount();
        int scrapCount = p.getScrapCount();
        int viewCount = p.getViewCount();
        int score = (p.getScore() == null) ? 0 : p.getScore();

        String displayName = (p.getUser() != null && StringUtils.hasText(p.getUser().getUsername()))
                ? p.getUser().getUsername()
                : "익명";

        String categoryStr = p.getCategory();
        String levelStr = p.getLevel();

        return new PostListDto(
                p.getPostId(),
                p.getTitle(),
                displayName,
                categoryStr,
                p.getGrade(),
                levelStr,
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

    private record JobMap(String category, String level) {}

    private JobMap mapJob(String job) {
        if (!StringUtils.hasText(job)) return new JobMap(null, null);
        String j = job.trim().toUpperCase();
        return switch (j) {
            case "BACKEND" -> new JobMap("BACKEND", null);
            case "FRONTEND" -> new JobMap("FRONTEND", null);
            case "FULLSTACK" -> new JobMap("FULLSTACK", null);
            case "DEVOPS" -> new JobMap("DEVOPS", null);
            case "DATAAI", "DATA_AI", "DATA/AI" -> new JobMap("DATA_AI", null);
            case "JUNIOR" -> new JobMap(null, "JUNIOR");
            case "MID", "MIDLEVEL", "MID_LEVEL" -> new JobMap(null, "MID_LEVEL");
            case "SENIOR" -> new JobMap(null, "SENIOR");
            default -> new JobMap(null, null);
        };
    }

    private static String firstNonBlank(String... v) {
        if (v == null) return null;
        for (String s : v) {
            if (s != null) {
                String t = s.trim();
                if (!t.isEmpty()) return t;
            }
        }
        return null;
    }

    private JobPosition toJobPosition(String s) {
        if (!StringUtils.hasText(s) || "전체".equalsIgnoreCase(s)) return null;
        try {
            return JobPosition.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return JobPosition.fromString(s);
        }
    }

    private CareerLevel toCareerLevel(String s) {
        if (!StringUtils.hasText(s) || "전체".equalsIgnoreCase(s)) return null;
        String v = s.trim().toUpperCase();
        if ("MID".equals(v) || "MIDLEVEL".equals(v)) v = "MID_LEVEL";
        try {
            return CareerLevel.valueOf(v);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Specification<CommunityPosts> titleContains(String keyword) {
        if (!StringUtils.hasText(keyword)) return null;
        String like = "%" + keyword.trim().toLowerCase() + "%";
        return (root, cq, cb) -> cb.like(cb.lower(root.get("title")), like);
    }

    @Transactional(readOnly = true)
    public Page<PostListDto> getPosts(Pageable pageable, JobPosition category, CareerLevel level, String qTitle) {
        Pageable safe = sanitizePageable(pageable, "createdAt", Sort.Direction.DESC, POST_SORT_WHITELIST);
        Specification<CommunityPosts> spec = (root, q, cb) -> cb.isFalse(root.get("deleted"));

        if (category != null) {
            String cat = category.name();
            spec = spec.and((r, q2, cb2) -> cb2.equal(cb2.upper(r.get("category")), cat));
        }
        if (level != null) {
            String lvl = level.name();
            spec = spec.and((r, q2, cb2) -> cb2.equal(cb2.upper(r.get("level")), lvl));
        }
        if (StringUtils.hasText(qTitle)) {
            spec = spec.and(titleContains(qTitle));
        }
        return postsRepository.findAll(spec, safe).map(this::toPostListDto);
    }
}
