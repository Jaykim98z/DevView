package com.allinone.DevView.community.controller;

import com.allinone.DevView.community.dto.*;
import com.allinone.DevView.community.entity.*;
import com.allinone.DevView.community.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.allinone.DevView.security.CustomUserDetails;

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
            @RequestParam(required = false) String level
    ) {
        boolean noFilter = (category == null || category.isBlank() || "전체".equals(category))
                && (level == null || level.isBlank() || "전체".equals(level));
        if (noFilter) {
            return ResponseEntity.ok(communityQueryService.getPosts(pageable));
        }
        return ResponseEntity.ok(communityQueryService.getPosts(pageable, category, level));
    }

    @GetMapping("/posts/dto")
    public ResponseEntity<Page<CommunityPostsDto>> listPostsAsDto(
            @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String level
    ) {
        boolean noFilter = (category == null || category.isBlank() || "전체".equals(category))
                && (level == null || level.isBlank() || "전체".equals(level));
        Page<PostListDto> page = noFilter
                ? communityQueryService.getPosts(pageable)
                : communityQueryService.getPosts(pageable, category, level);
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

}
