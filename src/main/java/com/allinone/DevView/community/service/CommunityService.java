package com.allinone.DevView.community.service;

import com.allinone.DevView.community.dto.CreateInterviewSharePostRequest;
import com.allinone.DevView.community.dto.CreatePostRequest;
import com.allinone.DevView.community.dto.CommunityPostDetailDto;
import com.allinone.DevView.community.dto.CommunityPostsDto;
import com.allinone.DevView.community.dto.PostUpdateRequestDto;
import com.allinone.DevView.community.entity.Comments;
import com.allinone.DevView.community.entity.CommunityPosts;
import com.allinone.DevView.community.entity.Likes;
import com.allinone.DevView.community.entity.LikesId;
import com.allinone.DevView.community.entity.Scraps;
import com.allinone.DevView.community.repository.CommentsRepository;
import com.allinone.DevView.community.repository.CommunityPostsRepository;
import com.allinone.DevView.community.repository.LikesRepository;
import com.allinone.DevView.community.repository.ScrapsRepository;
import com.allinone.DevView.common.enums.Grade;
import com.allinone.DevView.common.enums.InterviewType;
import com.allinone.DevView.user.entity.User;
import com.allinone.DevView.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityService {

    private static final String TYPE_INTERVIEW_SHARE = "INTERVIEW_SHARE";

    private final CommunityPostsRepository postsRepository;
    private final CommentsRepository commentsRepository;
    private final LikesRepository likesRepository;
    private final ScrapsRepository scrapsRepository;
    private final UserRepository userRepository;

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
        if (existingPostOpt.isEmpty()) throw new RuntimeException("Post not found");

        CommunityPosts existingPost = existingPostOpt.get();
        existingPost.setTitle(updatedPost.getTitle());
        existingPost.setContent(updatedPost.getContent());
        return postsRepository.save(existingPost);
    }

    @Transactional
    public Long createPost(CreatePostRequest req, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        final String content = req.content();
        final String summary = (content != null && content.length() > 1000) ? content.substring(0, 1000) : content;
        final InterviewType interviewType = parseInterviewType(req.interviewType(), InterviewType.TECHNICAL);
        final Grade grade = parseGrade(req.grade(), null);

        CommunityPosts post = new CommunityPosts();
        post.setUser(user);
        post.setWriterName(user.getUsername());
        post.setTitle(req.title());
        post.setContent(content);
        post.setSummary(summary);
        post.setInterviewType(interviewType.name());
        if (grade != null) post.setGrade(grade);
        post.setTechTag(req.techTag());
        post.setLevel(req.level());
        post.setCategory(req.category());
        post.setType(req.type());
        post.setScore(req.score() != null ? req.score() : 0);
        post.setLikeCount(0);
        post.setScrapCount(0);
        post.setViewCount(0);
        post.setCreatedAt(LocalDateTime.now());

        return postsRepository.save(post).getPostId();
    }

    @Transactional
    public Long createInterviewSharePost(CreateInterviewSharePostRequest req, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        if (req.getInterviewResultId() != null && postsRepository.existsByInterviewResultId(req.getInterviewResultId())) {
            throw new IllegalStateException("해당 인터뷰 결과는 이미 공유되었습니다.");
        }

        final String content = req.getContent();
        final String summary = (content != null && content.length() > 1000) ? content.substring(0, 1000) : content;

        CommunityPosts post = new CommunityPosts();
        post.setUser(user);
        post.setWriterName(user.getUsername());
        post.setType(TYPE_INTERVIEW_SHARE);
        post.setInterviewType(InterviewType.COMPREHENSIVE.name());
        post.setTitle(req.getTitle());
        post.setContent(content);
        post.setSummary(summary);
        if (req.getGrade() != null) post.setGrade(req.getGrade());
        try {
            post.setScore(req.getScore());
        } catch (Throwable ignore) {
            post.setScore(0);
        }
        post.setInterviewFeedback(req.getInterviewFeedback());
        post.setInterviewResultId(req.getInterviewResultId());
        post.setLikeCount(0);
        post.setScrapCount(0);
        post.setViewCount(0);
        post.setCreatedAt(LocalDateTime.now());

        return postsRepository.save(post).getPostId();
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

    @Transactional
    public CommunityPostDetailDto getPostDetailDto(Long postId) {
        postsRepository.incrementViewCount(postId);
        CommunityPosts post = postsRepository.findByIdWithUser(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글 없음: " + postId));
        return CommunityPostDetailDto.from(post);
    }

    @Transactional
    public long increaseViewCount(Long postId) {
        postsRepository.incrementViewCount(postId);
        return 0;
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

    /** 목록 DTO 변환 (InterviewType 라벨 포함) */
    public List<CommunityPostsDto> getAllPostDtos() {
        List<CommunityPosts> posts = postsRepository.findAllWithUser();
        return posts.stream().map(post -> {
            var user = post.getUser();
            Long uId = (user != null) ? user.getUserId() : null;
            String username = (user != null) ? user.getUsername() : "탈퇴한 사용자";

            if (user == null) log.warn("게시글 ID {} 는 유저 정보가 없습니다.", post.getPostId());

            String summary = post.getContent() != null && post.getContent().length() > 100
                    ? post.getContent().substring(0, 100) + "..."
                    : post.getContent();

            InterviewType interviewType = parseInterviewType(post.getInterviewType(), InterviewType.COMPREHENSIVE);
            String interviewTypeLabel = switch (interviewType) {
                case TECHNICAL -> "기술면접";
                case PRACTICAL -> "실무면접";
                case BEHAVIORAL -> "인성면접";
                case COMPREHENSIVE -> "종합면접";
            };

            Grade grade = post.getGrade();

            return new CommunityPostsDto(
                    post.getPostId(),
                    uId,
                    username,
                    post.getTechTag(),
                    post.getLevelTag(),
                    post.getTitle(),
                    summary,
                    post.getContent(),
                    interviewType,
                    interviewTypeLabel,
                    post.getScore(),
                    grade,
                    post.getViewCount(),
                    post.getLikeCount(),
                    post.getScrapCount(),
                    false,
                    false,
                    null,
                    post.getCreatedAt(),
                    post.getInterviewResultId(),
                    post.getInterviewFeedback()
            );
        }).toList();
    }

    @Transactional
    public Long updatePost(Long postId, Long requestUserId, PostUpdateRequestDto updated) {
        CommunityPosts post = postsRepository.findActiveByPostId(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!isOwner(post, requestUserId) && !hasRoleAdmin()) {
            throw new AccessDeniedException("수정 권한이 없습니다.");
        }

        post.update(updated.getTitle(), updated.getContent());
        return post.getPostId();
    }

    @Transactional
    public void deletePost(Long postId, Long requestUserId) {
        CommunityPosts post = postsRepository.findActiveByPostId(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!isOwner(post, requestUserId) && !hasRoleAdmin()) {
            throw new AccessDeniedException("삭제 권한이 없습니다.");
        }

        post.softDelete();
    }

    private boolean isOwner(CommunityPosts post, Long userId) {
        return post.getUser() != null && post.getUser().getUserId() != null
                && post.getUser().getUserId().equals(userId);
    }

    private boolean hasRoleAdmin() {
        return false;
    }

    private static InterviewType parseInterviewType(String raw, InterviewType def) {
        if (raw == null) return def;
        try {
            return InterviewType.valueOf(raw.trim().toUpperCase());
        } catch (Exception e) {
            return def;
        }
    }

    private static Grade parseGrade(String raw, Grade def) {
        if (raw == null) return def;
        try {
            return Grade.valueOf(raw.trim().toUpperCase());
        } catch (Exception e) {
            return def;
        }
    }
}
