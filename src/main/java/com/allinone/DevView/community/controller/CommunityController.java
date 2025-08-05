package com.allinone.DevView.community.controller;

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

    @GetMapping("/posts")
    public List<CommunityPosts> getAllPosts() {
        return communityService.getAllPosts();
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

    @PutMapping("/posts/{id}")
    public CommunityPosts updatePost(@PathVariable Long id, @RequestBody CommunityPosts post) {
        return communityService.updatePost(id, post);
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        communityService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/posts/{postId}/comments")
    public List<Comments> getComments(@PathVariable Long postId) {
        return communityService.getCommentsByPostId(postId);
    }

    @PostMapping("/posts/{postId}/comments")
    public Comments createComment(@PathVariable Long postId, @RequestBody Comments comment) {
        comment.setPostId(postId);
        return communityService.createComment(comment);
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        communityService.deleteComment(id);
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
}
