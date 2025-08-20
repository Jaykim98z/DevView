package com.allinone.DevView.community.service;

import com.allinone.DevView.community.dto.*;
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
import com.allinone.DevView.interview.dto.response.InterviewResultResponse;
import com.allinone.DevView.interview.entity.Interview;
import com.allinone.DevView.interview.entity.InterviewResult;
import com.allinone.DevView.interview.repository.InterviewResultRepository;
import com.allinone.DevView.user.entity.User;
import com.allinone.DevView.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
    private final InterviewResultRepository interviewResultRepository;


    public List<CommunityPosts> getAllPostsWithUserData() {
        return postsRepository.findAllWithUser();
    }

    public CommunityPosts getPostById(Long postId) {
        return postsRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글 없음: " + postId));
    }

    @Transactional
    public Long createPost(CreatePostRequest req, Long userId) {
        User user = findUser(userId);

        final String content = req.getContent();
        final String summary = summaryOf(content, 1000);

        final InterviewType interviewType = parseInterviewType(req.getInterviewType(), InterviewType.TECHNICAL);
        final Grade grade = parseGrade(req.getGrade(), null);

        CommunityPosts post = new CommunityPosts();
        post.setUser(user);
        post.setWriterName(user.getUsername());
        post.setTitle(req.getTitle());
        post.setContent(content);
        post.setSummary(summary);
        post.setInterviewType(interviewType.name());
        if (grade != null) post.setGrade(grade);
        post.setTechTag(req.getTechTag());
        post.setLevelTag(req.getLevel());
        post.setCategory(req.getCategory());
        post.setType(req.getType());
        post.setScore(req.getScore() != null ? req.getScore() : 0);
        post.setLikeCount(0);
        post.setScrapCount(0);
        post.setViewCount(0);
        post.setCreatedAt(LocalDateTime.now());

        return postsRepository.save(post).getPostId();
    }

    @Transactional
    public Long createInterviewSharePost(CreateInterviewSharePostRequest req, Long userId) {
        User user = findUser(userId);

        validateResultNotShared(req.getInterviewResultId());
        InterviewResult result = findOwnedResult(userId, req.getInterviewResultId());

        final String content = req.getContent();
        final String summary = summaryOf(content, 1000);
        final int resultScore = result.getTotalScore();

        CommunityPosts post = new CommunityPosts();
        post.setUser(user);
        post.setWriterName(user.getUsername());
        post.setType(TYPE_INTERVIEW_SHARE);
        post.setInterviewType(InterviewType.COMPREHENSIVE.name());
        post.setTitle(req.getTitle());
        post.setContent(content);
        post.setSummary(summary);

        if (req.getGrade() != null) {
            post.setGrade(parseGrade(req.getGrade().name(), result.getGrade()));
        } else {
            post.setGrade(result.getGrade());
        }

        post.setScore(req.getScore() != null ? req.getScore() : resultScore);

        post.setInterviewFeedback(firstNonBlank(req.getInterviewFeedback(), result.getFeedback()));

        post.setInterviewResultId(result.getId());
        post.setLikeCount(0);
        post.setScrapCount(0);
        post.setViewCount(0);
        post.setCreatedAt(LocalDateTime.now());

        return postsRepository.save(post).getPostId();
    }

    @Transactional
    public Long createInterviewSharePost(CreateInterviewSharePostRequest share,
                                         CreatePostRequest free,
                                         Long userId) {
        User user = findUser(userId);

        validateResultNotShared(share.getInterviewResultId());
        InterviewResult result = findOwnedResult(userId, share.getInterviewResultId());

        final int resultScore = result.getTotalScore();

        String title = firstNonBlank(
                share.getTitle(),
                free.getTitle(),
                "[면접결과] " + safe(result.getGrade()) + " " + safe(resultScore) + "점"
        );

        String content = firstNonBlank(
                free.getContent(),
                share.getContent(),
                result.getFeedback()
        );
        String summary = summaryOf(content, 1000);

        InterviewType interviewType = parseInterviewType(
                firstNonBlank(free.getInterviewType(), InterviewType.COMPREHENSIVE.name()),
                InterviewType.COMPREHENSIVE
        );

        Grade grade = firstNonNull(
                share.getGrade(),
                parseGrade(free.getGrade(), null),
                result.getGrade()
        );

        Integer score = firstNonNull(share.getScore(), free.getScore(), resultScore);

        CommunityPosts post = new CommunityPosts();
        post.setUser(user);
        post.setWriterName(user.getUsername());
        post.setType(TYPE_INTERVIEW_SHARE);
        post.setInterviewType(interviewType.name());
        post.setTitle(title);
        post.setContent(content);
        post.setSummary(summary);
        if (grade != null) post.setGrade(grade);
        post.setScore(score != null ? score : 0);
        post.setInterviewFeedback(firstNonBlank(share.getInterviewFeedback(), result.getFeedback()));
        post.setInterviewResultId(result.getId());

        post.setTechTag(free.getTechTag());
        post.setLevelTag(free.getLevel());
        post.setCategory(free.getCategory());

        post.setLikeCount(0);
        post.setScrapCount(0);
        post.setViewCount(0);
        post.setCreatedAt(LocalDateTime.now());

        return postsRepository.save(post).getPostId();
    }


    @Transactional
    public Long updatePost(Long postId, Long requestUserId, PostUpdateRequestDto updated) {
        CommunityPosts post = postsRepository.findActiveByPostId(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        if (!isOwner(post, requestUserId) && !hasRoleAdmin()) {
            throw new AccessDeniedException("수정 권한이 없습니다.");
        }
        post.update(updated.getTitle(), updated.getContent());
        if (updated.getGrade() != null) {
            post.setGrade(updated.getGrade());
        }
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
    public LikesDto addLike(Long userId, Long postId) {
        if (likesRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new RuntimeException("Already liked");
        }
        Likes like = new Likes();
        like.setUserId(userId);
        like.setPostId(postId);
        Likes saved = likesRepository.save(like);
        return new LikesDto(saved.getUserId(), saved.getPostId());
    }

    @Transactional
    public void removeLike(Long userId, Long postId) {
        LikesId likeId = new LikesId();
        likeId.setUserId(userId);
        likeId.setPostId(postId);
        likesRepository.deleteById(likeId);
    }

    @Transactional
    public ScrapsDto addScrap(Long userId, Long postId) {
        if (scrapsRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new RuntimeException("Already scrapped");
        }
        Scraps scrap = new Scraps();
        scrap.setUserId(userId);
        scrap.setPostId(postId);
        Scraps saved = scrapsRepository.save(scrap);
        return new ScrapsDto(saved.getScrapId(), saved.getUserId(), saved.getPostId());
    }

    @Transactional
    public void removeScrap(Long userId, Long postId) {
        scrapsRepository.deleteByUserIdAndPostId(userId, postId);
    }

    @Transactional
    public long increaseViewCount(Long postId) {
        postsRepository.incrementViewCount(postId);
        return postsRepository.findById(postId)
                .map(CommunityPosts::getViewCount)
                .orElse(0);
    }

    @Transactional
    public CommunityPostDetailDto getPostDetailDto(Long postId) {
        postsRepository.incrementViewCount(postId);
        CommunityPosts post = postsRepository.findByIdWithUser(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글 없음: " + postId));
        return CommunityPostDetailDto.from(post);
    }

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
            String gradeStr = (post.getGrade() != null ? post.getGrade().name() : null);
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
                    gradeStr,
                    post.getViewCount(),
                    post.getLikeCount(),
                    post.getScrapCount(),
                    false,
                    false,
                    null,
                    post.getCreatedAt()
            );
        }).toList();
    }


    @Transactional
    public InterviewResultResponse getLatestInterviewResult(Long userId) {
        return interviewResultRepository.findByUserId(userId).stream()
                .findFirst()
                .map(InterviewResultResponse::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("인터뷰 결과가 없습니다."));
    }

    @Transactional
    public List<InterviewResultResponse> getAllInterviewResults(Long userId) {
        return interviewResultRepository.findByUserId(userId).stream()
                .map(InterviewResultResponse::fromEntity)
                .toList();
    }

    @Transactional
    public InterviewResultResponse getInterviewResultById(Long userId, Long resultId) {
        InterviewResult result = findOwnedResult(userId, resultId);
        return InterviewResultResponse.fromEntity(result);
    }


    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
    }

    private void validateResultNotShared(Long resultId) {
        if (resultId != null && postsRepository.existsByInterviewResultId(resultId)) {
            throw new IllegalStateException("해당 인터뷰 결과는 이미 공유되었습니다.");
        }
    }

    private InterviewResult findOwnedResult(Long userId, Long resultId) {
        if (resultId == null) {
            throw new IllegalArgumentException("인터뷰 결과 ID가 필요합니다.");
        }
        InterviewResult result = interviewResultRepository.findById(resultId)
                .orElseThrow(() -> new IllegalArgumentException("인터뷰 결과를 찾을 수 없습니다: " + resultId));
        Interview interview = result.getInterview();
        if (interview == null || interview.getUser() == null ||
                !userId.equals(interview.getUser().getUserId())) {
            throw new AccessDeniedException("본인의 인터뷰 결과만 공유할 수 있습니다.");
        }
        return result;
    }

    private boolean isOwner(CommunityPosts post, Long userId) {
        return post.getUser() != null
                && post.getUser().getUserId() != null
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

    private static String summaryOf(String content, int max) {
        if (content == null) return null;
        return (content.length() > max) ? content.substring(0, max) : content;
    }

    private static String safe(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    @SafeVarargs
    private static <T> T firstNonNull(T... values) {
        for (T v : values) if (v != null) return v;
        return null;
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.trim().isEmpty()) return v.trim();
        }
        return null;
    }
}
