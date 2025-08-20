package com.allinone.DevView.community.controller;

import com.allinone.DevView.community.dto.*;
import com.allinone.DevView.community.service.CommunityQueryService;
import com.allinone.DevView.community.service.CommunityService;
import com.allinone.DevView.interview.dto.response.InterviewResultResponse;
import com.allinone.DevView.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityQueryService communityQueryService;
    private final CommunityService communityService;

    @GetMapping("/posts")
    public ResponseEntity<Page<PostListDto>> listPosts(
            @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(communityQueryService.getPosts(pageable));
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<Object> getPost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long viewerId = (user != null) ? user.getUserId() : null;
        return communityQueryService.getPostDetail(postId, viewerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/posts")
    public ResponseEntity<Map<String, Long>> createPost(
            @Valid @RequestBody CreatePostRequest req,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long userId = user.getUserId();
        Long postId = communityService.createPost(req, userId);
        return ResponseEntity
                .created(URI.create("/api/community/posts/" + postId))
                .body(Map.of("postId", postId));
    }

    @PostMapping("/posts/interview")
    public ResponseEntity<Map<String, Long>> createInterviewShare(
            @Valid @RequestBody CreateInterviewSharePostRequest req,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long userId = user.getUserId();
        Long postId = communityService.createInterviewSharePost(req, userId);
        return ResponseEntity
                .created(URI.create("/api/community/posts/" + postId))
                .body(Map.of("postId", postId));
    }

    @PatchMapping("/posts/{postId}")
    public ResponseEntity<Long> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequestDto req,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long updatedId = communityService.updatePost(postId, user.getUserId(), req);
        return ResponseEntity.ok(updatedId);
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        communityService.deletePost(postId, user.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/posts/{postId}/likes")
    public ResponseEntity<LikesDto> addLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        LikesDto like = communityService.addLike(user.getUserId(), postId);
        return ResponseEntity.status(HttpStatus.CREATED).body(like);
    }

    @DeleteMapping("/posts/{postId}/likes")
    public ResponseEntity<Void> removeLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        communityService.removeLike(user.getUserId(), postId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/posts/{postId}/scraps")
    public ResponseEntity<ScrapsDto> addScrap(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ScrapsDto scrap = communityService.addScrap(user.getUserId(), postId);
        return ResponseEntity.status(HttpStatus.CREATED).body(scrap);
    }

    @DeleteMapping("/posts/{postId}/scraps")
    public ResponseEntity<Void> removeScrap(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        communityService.removeScrap(user.getUserId(), postId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/posts/{postId}/views")
    public ResponseEntity<Map<String, Long>> increaseView(@PathVariable Long postId) {
        long cnt = communityService.increaseViewCount(postId);
        return ResponseEntity.ok(Map.of("viewCount", cnt));
    }


    @GetMapping("/interview-results")
    public ResponseEntity<List<InterviewResultResponse>> getMyInterviewResults(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        List<InterviewResultResponse> list = communityService.getAllInterviewResults(user.getUserId());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/interview-results/latest")
    public ResponseEntity<InterviewResultResponse> getMyLatestInterviewResult(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        try {
            InterviewResultResponse latest = communityService.getLatestInterviewResult(user.getUserId());
            return ResponseEntity.ok(latest);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.noContent().build();
        }
    }

    @GetMapping("/interview-results/{resultId}")
    public ResponseEntity<InterviewResultResponse> getMyInterviewResultById(
            @PathVariable Long resultId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        try {
            InterviewResultResponse dto =
                    communityService.getInterviewResultById(user.getUserId(), resultId);
            return ResponseEntity.ok(dto);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
