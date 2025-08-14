package com.allinone.DevView.community.controller;

import com.allinone.DevView.community.dto.CommunityPostsDto;
import com.allinone.DevView.community.dto.CreateInterviewSharePostRequest;
import com.allinone.DevView.community.dto.PostListDto;
import com.allinone.DevView.community.entity.CommunityPosts;
import com.allinone.DevView.community.entity.Likes;
import com.allinone.DevView.community.entity.Scraps;
import com.allinone.DevView.community.service.CommunityQueryService;
import com.allinone.DevView.community.service.CommunityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
