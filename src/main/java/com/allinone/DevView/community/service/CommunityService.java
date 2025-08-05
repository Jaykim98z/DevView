package com.allinone.DevView.community.service;

import com.allinone.DevView.community.entity.CommunityPosts;
import com.allinone.DevView.community.entity.Comments;
import com.allinone.DevView.community.entity.Likes;
import com.allinone.DevView.community.entity.LikesId;
import com.allinone.DevView.community.entity.Scraps;
import com.allinone.DevView.community.repository.CommunityPostsRepository;
import com.allinone.DevView.community.repository.CommentsRepository;
import com.allinone.DevView.community.repository.LikesRepository;
import com.allinone.DevView.community.repository.ScrapsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityPostsRepository postsRepository;
    private final CommentsRepository commentsRepository;
    private final LikesRepository likesRepository;
    private final ScrapsRepository scrapsRepository;

    public List<CommunityPosts> getAllPosts() {
        return postsRepository.findAll();
    }

    public Optional<CommunityPosts> getPostById(Long postId) {
        return postsRepository.findById(postId);
    }

    @Transactional
    public CommunityPosts createPost(CommunityPosts post) {
        return postsRepository.save(post);
    }

    @Transactional
    public CommunityPosts updatePost(Long postId, CommunityPosts updatedPost) {
        Optional<CommunityPosts> existingPostOpt = postsRepository.findById(postId);
        if (existingPostOpt.isEmpty()) {
            throw new RuntimeException("Post not found");
        }
        CommunityPosts existingPost = existingPostOpt.get();
        existingPost.setTitle(updatedPost.getTitle());
        existingPost.setContent(updatedPost.getContent());
        return postsRepository.save(existingPost);
    }

    @Transactional
    public void deletePost(Long postId) {
        postsRepository.deleteById(postId);
    }

    public List<Comments> getCommentsByPostId(Long postId) {
        return commentsRepository.findByPostIdOrderByCreatedAtAsc(postId);
    }

    @Transactional
    public Comments createComment(Comments comment) {
        return commentsRepository.save(comment);
    }

    @Transactional
    public void deleteComment(Long commentId) {
        commentsRepository.deleteById(commentId);
    }

    @Transactional
    public Likes addLike(Long userId, Long postId) {
        LikesId likeId = new LikesId();
        likeId.setUserId(userId);
        likeId.setPostId(postId);
        if (likesRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new RuntimeException("Already liked");
        }
        Likes like = new Likes();
        like.setUserId(userId);
        like.setPostId(postId);
        return likesRepository.save(like);
    }

    @Transactional
    public void removeLike(Long userId, Long postId) {
        LikesId likeId = new LikesId();
        likeId.setUserId(userId);
        likeId.setPostId(postId);
        likesRepository.deleteById(likeId);
    }


    @Transactional
    public Scraps addScrap(Scraps scrap) {
        return scrapsRepository.save(scrap);
    }

    @Transactional
    public void removeScrap(Long scrapId) {
        scrapsRepository.deleteById(scrapId);
    }

    public List<Likes> getLikesByUserId(Long userId) {
        return likesRepository.findByUserId(userId);
    }

    public long countLikesByPostId(Long postId) {
        return likesRepository.findByPostId(postId).size();
    }

    public List<Scraps> getScrapsByUserId(Long userId) {
        return scrapsRepository.findByUserId(userId);
    }

}
