package com.allinone.DevView.community.service;

import com.allinone.DevView.community.dto.CommunityPostDetailDto;
import com.allinone.DevView.community.dto.CommunityPostsDto;
import com.allinone.DevView.community.entity.Comments;
import com.allinone.DevView.community.entity.CommunityPosts;
import com.allinone.DevView.community.entity.Likes;
import com.allinone.DevView.community.entity.LikesId;
import com.allinone.DevView.community.entity.Scraps;
import com.allinone.DevView.community.repository.CommentsRepository;
import com.allinone.DevView.community.repository.CommunityPostsRepository;
import com.allinone.DevView.community.repository.LikesRepository;
import com.allinone.DevView.community.repository.ScrapsRepository;
import com.allinone.DevView.common.enums.InterviewType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityPostsRepository postsRepository;
    private final CommentsRepository commentsRepository;
    private final LikesRepository likesRepository;
    private final ScrapsRepository scrapsRepository;

    public List<CommunityPosts> getAllPostsWithUserData() {
        return postsRepository.findAllWithUser();
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

    public CommunityPostDetailDto getPostDetailDto(Long postId) {
        CommunityPosts post = postsRepository.findByIdWithUser(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글 없음: " + postId));
        return CommunityPostDetailDto.from(post);
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

    public List<CommunityPostsDto> getAllPostDtos() {
        List<CommunityPosts> posts = postsRepository.findAllWithUser();

        return posts.stream().map(post -> {
            var user = post.getUser();

            Long userId = (user != null) ? user.getUserId() : null;
            String username = (user != null) ? user.getUsername() : "탈퇴한 사용자";

            if (user == null) {
                log.warn("게시글 ID {} 는 유저 정보가 없습니다.", post.getPostId());
            }

            String summary = post.getContent() != null && post.getContent().length() > 100
                    ? post.getContent().substring(0, 100) + "..."
                    : post.getContent();

            InterviewType interviewType;
            try {
                interviewType = InterviewType.valueOf(post.getInterviewType());
            } catch (IllegalArgumentException | NullPointerException e) {
                interviewType = InterviewType.GENERAL;
            }

            String interviewTypeLabel = switch (interviewType) {
                case PRACTICE -> "기술면접";
                case REAL     -> "실무면접";
                case GENERAL  -> "종합면접";
                case HR       -> "인성면접";
                default       -> "기타";
            };

            return new CommunityPostsDto(
                    post.getPostId(),
                    userId,
                    username,
                    post.getTechTag(),
                    post.getLevelTag(),
                    post.getTitle(),
                    summary,
                    post.getContent(),
                    interviewType,
                    interviewTypeLabel,
                    post.getScore(),
                    post.getGrade(),
                    post.getViewCount(),
                    post.getLikeCount(),
                    post.getScrapCount(),
                    false,  // liked
                    false,  // bookmarked
                    null,   // scrapId
                    post.getCreatedAt()
            );
        }).toList();
    }
}
