package com.allinone.DevView.community.service;

import com.allinone.DevView.common.enums.Grade;
import com.allinone.DevView.common.enums.InterviewType;
import com.allinone.DevView.community.dto.CommunityPostsDto;
import com.allinone.DevView.community.dto.CreateInterviewSharePostRequest;
import com.allinone.DevView.community.dto.CreatePostRequest;
import com.allinone.DevView.community.dto.PostListDto;
import com.allinone.DevView.community.dto.PostUpdateRequestDto;
import com.allinone.DevView.community.entity.CommunityPosts;
import com.allinone.DevView.community.entity.Likes;
import com.allinone.DevView.community.entity.Scraps;
import com.allinone.DevView.community.repository.CommunityPostsRepository;
import com.allinone.DevView.user.entity.User;
import com.allinone.DevView.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityPostsRepository postsRepository;
    private final UserRepository userRepository;

    public Page<PostListDto> getPosts(Pageable pageable) {
        throw new UnsupportedOperationException("Query service 분리됨");
    }

    public List<CommunityPostsDto> getAllPostDtos() {
        throw new UnsupportedOperationException("레거시 DTO 조회는 QueryService로 이전 권장");
    }

    public Optional<CommunityPosts> getPostById(Long id) {
        return postsRepository.findById(id);
    }

    public CommunityPosts createPost(CommunityPosts post) {
        if (post.getUser() == null || post.getUser().getUserId() == null) throw new IllegalArgumentException("user가 필요합니다.");
        if (isBlank(post.getTitle())) throw new IllegalArgumentException("제목을 입력해주세요.");
        if (isBlank(post.getContent())) throw new IllegalArgumentException("내용을 입력해주세요.");
        if (post.getGrade() == null) post.setGrade(Grade.C);
        if (isBlank(post.getInterviewType())) post.setInterviewType(InterviewType.PRACTICAL.name());
        post.setScore(clampScore(post.getScore()));
        if (isBlank(post.getType())) post.setType("POST");
        return postsRepository.save(post);
    }

    public Long createInterviewSharePost(CreateInterviewSharePostRequest dto, Long userId) {
        if (dto == null) throw new IllegalArgumentException("인터뷰 공유 글 데이터가 비었습니다.");
        if (isBlank(dto.getTitle())) throw new IllegalArgumentException("제목을 입력해주세요.");
        if (isBlank(dto.getContent())) throw new IllegalArgumentException("내용을 입력해주세요.");
        if (dto.getGrade() == null) throw new IllegalArgumentException("grade 값이 필요합니다.");
        if (isBlank(dto.getInterviewFeedback())) throw new IllegalArgumentException("interviewFeedback 값이 필요합니다.");

        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        CommunityPosts post = new CommunityPosts();
        post.setUser(user);
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setGrade(dto.getGrade());
        post.setInterviewType(normalizeInterviewType(String.valueOf(dto.getInterviewType())));
        post.setScore(clampScore(dto.getScore()));
        post.setInterviewFeedback(dto.getInterviewFeedback());
        post.setInterviewResultId(dto.getInterviewResultId());
        post.setCategory("INTERVIEW_SHARE");
        post.setType("POST");

        postsRepository.save(post);
        return post.getPostId();
    }

    public Long createFreePost(CreatePostRequest dto, Long userId) {
        if (dto == null) throw new IllegalArgumentException("자유 글 데이터가 비었습니다.");
        if (isBlank(dto.title())) throw new IllegalArgumentException("제목을 입력해주세요.");
        if (isBlank(dto.content())) throw new IllegalArgumentException("내용을 입력해주세요.");
        if (isBlank(dto.grade())) throw new IllegalArgumentException("grade 값이 필요합니다.");
        if (isBlank(dto.interviewType())) throw new IllegalArgumentException("interviewType 값이 필요합니다.");

        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        CommunityPosts post = new CommunityPosts();
        post.setUser(user);
        post.setTitle(dto.title());
        post.setContent(dto.content());
        post.setGrade(parseGrade(dto.grade()));
        post.setInterviewType(normalizeInterviewType(dto.interviewType()));
        post.setScore(clampScore(dto.score()));
        post.setTechTag(nvl(dto.techTag()));
        post.setLevel(nvl(dto.level()));
        post.setType(nvlOr(dto.type(), "POST"));
        post.setCategory(nvlOr(dto.category(), "FREE"));

        postsRepository.save(post);
        return post.getPostId();
    }

    public CommunityPosts updatePost(Long id, CommunityPosts src) {
        CommunityPosts post = postsRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));
        if (!isBlank(src.getTitle())) post.setTitle(src.getTitle());
        if (!isBlank(src.getContent())) post.setContent(src.getContent());
        if (src.getGrade() != null) post.setGrade(src.getGrade());
        if (!isBlank(src.getInterviewType())) post.setInterviewType(normalizeInterviewType(src.getInterviewType()));
        post.setScore(clampScore(src.getScore()));
        if (!isBlank(src.getSummary())) post.setSummary(src.getSummary());
        if (!isBlank(src.getTechTag())) post.setTechTag(src.getTechTag());
        if (!isBlank(src.getLevel())) post.setLevel(src.getLevel());
        if (!isBlank(src.getType())) post.setType(src.getType());
        if (!isBlank(src.getCategory())) post.setCategory(src.getCategory());
        return postsRepository.save(post);
    }

    public Long updatePost(Long postId, Long userId, PostUpdateRequestDto req) {
        CommunityPosts post = postsRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));
        if (!post.getUser().getUserId().equals(userId)) throw new IllegalArgumentException("수정 권한이 없습니다.");

        if (!isBlank(req.getTitle())) post.setTitle(req.getTitle());
        if (!isBlank(req.getContent())) post.setContent(req.getContent());
        if (req.getGrade() != null) post.setGrade(req.getGrade());

        postsRepository.save(post);
        return post.getPostId();
    }

    public void deletePost(Long id) {
        CommunityPosts post = postsRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));
        postsRepository.delete(post);
    }

    public void deletePost(Long id, Long userId) {
        CommunityPosts post = postsRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));
        if (!post.getUser().getUserId().equals(userId)) throw new IllegalArgumentException("삭제 권한이 없습니다.");
        post.softDelete();
        postsRepository.save(post);
    }

    public Likes addLike(Long userId, Long postId) {
        throw new UnsupportedOperationException("좋아요 서비스 구현체에 위임하세요");
    }

    public void removeLike(Long userId, Long postId) {
        throw new UnsupportedOperationException("좋아요 서비스 구현체에 위임하세요");
    }

    public Scraps addScrap(Scraps scrap) {
        throw new UnsupportedOperationException("스크랩 서비스 구현체에 위임하세요");
    }

    public void removeScrap(Long id) {
        throw new UnsupportedOperationException("스크랩 서비스 구현체에 위임하세요");
    }

    public long increaseViewCount(Long postId) {
        CommunityPosts post = postsRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));
        post.setViewCount(post.getViewCount() + 1);
        postsRepository.save(post);
        return post.getViewCount();
    }

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
    private static String nvl(String s) { return s == null ? "" : s; }
    private static String nvlOr(String s, String def) { return isBlank(s) ? def : s; }

    private static int clampScore(Integer score) {
        if (score == null) return 0;
        int v = score;
        if (v < 0) v = 0;
        if (v > 100) v = 100;
        return v;
    }

    private static int clampScore(int score) {
        if (score < 0) return 0;
        if (score > 100) return 100;
        return score;
    }

    private static Grade parseGrade(String v) {
        try {
            return Grade.valueOf(v.trim().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            throw new IllegalArgumentException("유효하지 않은 grade 값입니다: " + v);
        }
    }

    private static InterviewType toInterviewTypeEnum(String v) {
        if (isBlank(v)) return InterviewType.PRACTICAL;
        String norm = v.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        if ("PRACTICE".equals(norm)) norm = "PRACTICAL";
        try {
            return InterviewType.valueOf(norm);
        } catch (Exception e) {
            return InterviewType.PRACTICAL;
        }
    }

    private static String normalizeInterviewType(String v) {
        if (isBlank(v)) return InterviewType.PRACTICAL.name();
        String norm = v.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        if ("PRACTICE".equals(norm)) norm = "PRACTICAL";
        try {
            return InterviewType.valueOf(norm).name();
        } catch (Exception e) {
            return InterviewType.PRACTICAL.name();
        }
    }

    @Transactional(readOnly = true)
    public CommunityPostsDto getPostDetail(Long postId) {
        CommunityPosts post = postsRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. id=" + postId));
        return toPostsDto(post);
    }

    private CommunityPostsDto toPostsDto(CommunityPosts post) {
        CommunityPostsDto dto = new CommunityPostsDto();
        dto.setId(post.getPostId());
        dto.setUserId(post.getUser() != null ? post.getUser().getUserId() : null);
        dto.setUsername(post.getUser() != null ? post.getUser().getUsername() : null);
        dto.setProfileImage(null);
        dto.setTechTag(post.getTechTag());
        dto.setLevelTag(post.getLevel());
        dto.setTitle(post.getTitle());
        dto.setSummary(post.getSummary());
        dto.setContent(post.getContent());
        dto.setInterviewType(toInterviewTypeEnum(post.getInterviewType()));
        dto.setInterviewTypeLabel(post.getInterviewType() != null ? normalizeInterviewType(post.getInterviewType()) : null);
        dto.setScore(post.getScore());
        dto.setGrade(post.getGrade() != null ? post.getGrade().name() : null);
        dto.setViewCount(post.getViewCount());
        dto.setLikeCount(post.getLikeCount());
        dto.setScrapCount(post.getScrapCount());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setInterviewResultId(post.getInterviewResultId());
        dto.setInterviewFeedback(post.getInterviewFeedback());
        return dto;
    }
}
