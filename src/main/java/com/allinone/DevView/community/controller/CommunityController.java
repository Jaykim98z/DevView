package com.allinone.DevView.community.controller;

import com.allinone.DevView.community.dto.*;
import com.allinone.DevView.community.entity.*;
import com.allinone.DevView.community.service.*;
import com.allinone.DevView.security.CustomUserDetails;
import com.allinone.DevView.common.enums.JobPosition;
import com.allinone.DevView.common.enums.CareerLevel;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.*;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityQueryService communityQueryService;
    private final CommunityService communityService;

    @GetMapping("/posts")
    public ResponseEntity<Page<PostListDto>> listPosts(
            @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String jobCategory,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String careerLevel,
            @RequestParam(required = false) String job,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, name = "qTitle") String qTitle
    ) {
        String catStr = normalizeCategory(firstNonBlank(category, jobCategory));
        String lvlStr = normalizeLevel(firstNonBlank(level, careerLevel));

        if (qTitle != null && !qTitle.isBlank()) {
            JobPosition catEnumForTitle = toJobPosition(catStr);
            CareerLevel lvlEnumForTitle = toCareerLevel(lvlStr);
            return ResponseEntity.ok(
                    communityQueryService.getPosts(pageable, catEnumForTitle, lvlEnumForTitle, qTitle)
            );
        }

        boolean useSearch = (job != null && !job.isBlank()) || (keyword != null && !keyword.isBlank());
        if (useSearch) {
            return ResponseEntity.ok(
                    communityQueryService.getPostsByJob(pageable, job, catStr, lvlStr, keyword)
            );
        }

        JobPosition catEnum = toJobPosition(catStr);
        CareerLevel lvlEnum = toCareerLevel(lvlStr);

        if (catEnum == null && lvlEnum == null) {
            return ResponseEntity.ok(communityQueryService.getPosts(pageable));
        }
        return ResponseEntity.ok(communityQueryService.getPosts(pageable, catEnum, lvlEnum));
    }

    @GetMapping("/posts/dto")
    public ResponseEntity<Page<CommunityPostsDto>> listPostsAsDto(
            @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String jobCategory,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String careerLevel
    ) {
        String catStr = normalizeCategory(firstNonBlank(category, jobCategory));
        String lvlStr = normalizeLevel(firstNonBlank(level, careerLevel));
        JobPosition catEnum = toJobPosition(catStr);
        CareerLevel lvlEnum = toCareerLevel(lvlStr);

        Page<PostListDto> page = (catEnum == null && lvlEnum == null)
                ? communityQueryService.getPosts(pageable)
                : communityQueryService.getPosts(pageable, catEnum, lvlEnum);
        Page<CommunityPostsDto> converted = page.map(this::toCommunityPostsDto);
        return ResponseEntity.ok(converted);
    }

    @GetMapping("/posts/legacy")
    public ResponseEntity<List<CommunityPostsDto>> getAllPostDtos() {
        return ResponseEntity.ok(communityService.getAllPostDtos());
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<CommunityPosts> getPostById(@PathVariable Long id) {
        return communityService.getPostById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/posts")
    public CommunityPosts createPost(@RequestBody CommunityPosts post) {
        return communityService.createPost(post);
    }

    @PostMapping("/posts/interview/{userId}")
    public ResponseEntity<Map<String, Long>> createInterviewShare(
            @PathVariable Long userId,
            @Valid @RequestBody CreateInterviewSharePostRequest req
    ) {
        Long postId = communityService.createInterviewSharePost(req, userId);
        return ResponseEntity.ok(Map.of("postId", postId));
    }

    @PutMapping("/posts/{id}")
    public CommunityPosts updatePost(@PathVariable Long id, @RequestBody CommunityPosts post) {
        return communityService.updatePost(id, post);
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        communityService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/posts/{postId}/likes/{userId}")
    public Likes addLike(@PathVariable Long postId, @PathVariable Long userId) {
        return communityService.addLike(userId, postId);
    }

    @DeleteMapping("/posts/{postId}/likes/{userId}")
    public ResponseEntity<Void> removeLike(@PathVariable Long postId, @PathVariable Long userId) {
        communityService.removeLike(userId, postId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/posts/{postId}/scraps")
    public Scraps addScrap(@PathVariable Long postId, @RequestBody Scraps scrap) {
        scrap.setPostId(postId);
        return communityService.addScrap(scrap);
    }

    @DeleteMapping("/scraps/{id}")
    public ResponseEntity<Void> removeScrap(@PathVariable Long id) {
        communityService.removeScrap(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/posts/{postId}/view")
    public Map<String, Long> increaseView(@PathVariable Long postId) {
        long cnt = communityService.increaseViewCount(postId);
        return Map.of("viewCount", cnt);
    }

    @PatchMapping("/posts/{postId}")
    public ResponseEntity<?> patchUpdatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequestDto request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long id = communityService.updatePost(postId, user.getUserId(), request);
        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/posts/{postId}/soft")
    public ResponseEntity<?> softDeletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        communityService.deletePost(postId, user.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/posts/compose")
    public ResponseEntity<Map<String, Long>> createComposedPost(
            @Valid @RequestBody CombinedPostRequest req,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long userId = user == null ? null : user.getUserId();
        if (userId == null) return ResponseEntity.status(401).body(Map.of("message", -1L));

        CombinedPostRequest.PostCategory category = req.getCategory();
        if (category == null) {
            category = (req.getInterviewShare() != null)
                    ? CombinedPostRequest.PostCategory.INTERVIEW_SHARE
                    : CombinedPostRequest.PostCategory.FREE;
        }

        Long postId = switch (category) {
            case INTERVIEW_SHARE -> communityService.createInterviewSharePost(req.getInterviewShare(), userId);
            case FREE -> communityService.createFreePost(req.getFreePost(), userId);
        };
        return ResponseEntity.ok(Map.of("postId", postId));
    }

    private CommunityPostsDto toCommunityPostsDto(PostListDto src) {
        if (src == null) return null;
        CommunityPostsDto dst = new CommunityPostsDto();
        BeanUtils.copyProperties(src, dst);
        dst.setScore(src.score());
        dst.setGrade(src.grade() == null ? "--" : src.grade().toString());
        return dst;
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

    private static String normalizeCategory(String s) {
        if (s == null) return null;
        String k = s.trim().toLowerCase();
        return switch (k) {
            case "백엔드","backend" -> "BACKEND";
            case "프론트엔드","frontend" -> "FRONTEND";
            case "풀스택","fullstack" -> "FULLSTACK";
            case "devops","데브옵스" -> "DEVOPS";
            case "data/ai","data","ai" -> "DATA_AI";
            case "전체" -> "전체";
            default -> s.toUpperCase();
        };
    }

    private static String normalizeLevel(String s) {
        if (s == null) return null;
        String k = s.trim().toLowerCase();
        return switch (k) {
            case "주니어","junior" -> "JUNIOR";
            case "미드","미드레벨","mid","midlevel","mid_level" -> "MID_LEVEL";
            case "시니어","senior" -> "SENIOR";
            case "전체" -> "전체";
            default -> s.toUpperCase();
        };
    }

    private static JobPosition toJobPosition(String s) {
        if (s == null || "전체".equalsIgnoreCase(s)) return null;
        return JobPosition.fromString(s);
    }

    private static CareerLevel toCareerLevel(String s) {
        if (s == null || "전체".equalsIgnoreCase(s)) return null;
        try {
            return CareerLevel.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            if ("MID".equalsIgnoreCase(s) || "MIDLEVEL".equalsIgnoreCase(s) || "MID_LEVEL".equalsIgnoreCase(s)) {
                return CareerLevel.MID_LEVEL;
            }
            return null;
        }
    }
}
