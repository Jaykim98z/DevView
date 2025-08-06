package com.allinone.DevView.community.controller;

import com.allinone.DevView.community.dto.CommunityPostsDto;
import com.allinone.DevView.community.entity.Comments;
import com.allinone.DevView.community.entity.CommunityPosts;
import com.allinone.DevView.community.entity.Likes;
import com.allinone.DevView.community.entity.Scraps;
import com.allinone.DevView.community.service.CommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    // 게시글 목록 조회 (댓글 수 포함된 DTO)
    @GetMapping("/posts")
    public ResponseEntity<List<CommunityPostsDto>> getAllPostDtos() {
        return ResponseEntity.ok(communityService.getAllPostDtos());
    }

    // 단일 게시글 조회
    @GetMapping("/posts/{id}")
    public ResponseEntity<CommunityPosts> getPostById(@PathVariable Long id) {
        return communityService.getPostById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 게시글 생성
    @PostMapping("/posts")
    public CommunityPosts createPost(@RequestBody CommunityPosts post) {
        return communityService.createPost(post);
    }

    // 게시글 수정
    @PutMapping("/posts/{id}")
    public CommunityPosts updatePost(@PathVariable Long id, @RequestBody CommunityPosts post) {
        return communityService.updatePost(id, post);
    }

    // 게시글 삭제
    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        communityService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    // 댓글 목록 조회
    @GetMapping("/posts/{postId}/comments")
    public List<Comments> getComments(@PathVariable Long postId) {
        return communityService.getCommentsByPostId(postId);
    }

    // 댓글 작성
    @PostMapping("/posts/{postId}/comments")
    public Comments createComment(@PathVariable Long postId, @RequestBody Comments comment) {
        comment.setPostId(postId);
        return communityService.createComment(comment);
    }

    // 댓글 삭제
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        communityService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }

    // 좋아요 추가
    @PostMapping("/posts/{postId}/likes/{userId}")
    public Likes addLike(@PathVariable Long postId, @PathVariable Long userId) {
        return communityService.addLike(userId, postId);
    }

    // 좋아요 취소
    @DeleteMapping("/posts/{postId}/likes/{userId}")
    public ResponseEntity<Void> removeLike(@PathVariable Long postId, @PathVariable Long userId) {
        communityService.removeLike(userId, postId);
        return ResponseEntity.noContent().build();
    }

    // 스크랩 추가
    @PostMapping("/posts/{postId}/scraps")
    public Scraps addScrap(@PathVariable Long postId, @RequestBody Scraps scrap) {
        scrap.setPostId(postId);
        return communityService.addScrap(scrap);
    }

    // 스크랩 삭제
    @DeleteMapping("/scraps/{id}")
    public ResponseEntity<Void> removeScrap(@PathVariable Long id) {
        communityService.removeScrap(id);
        return ResponseEntity.noContent().build();
    }
}
