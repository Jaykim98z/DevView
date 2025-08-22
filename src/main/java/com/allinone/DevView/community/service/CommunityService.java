package com.allinone.DevView.community.service;

import com.allinone.DevView.community.dto.CommunityPostsDto;
import com.allinone.DevView.community.dto.CreateInterviewSharePostRequest;
import com.allinone.DevView.community.dto.CreatePostRequest;
import com.allinone.DevView.community.dto.PostListDto;
import com.allinone.DevView.community.dto.PostUpdateRequestDto;
import com.allinone.DevView.community.entity.CommunityPosts;
import com.allinone.DevView.community.entity.Likes;
import com.allinone.DevView.community.entity.Scraps;
import com.allinone.DevView.community.repository.CommunityPostsRepository;
import com.allinone.DevView.interview.entity.InterviewResult;
import com.allinone.DevView.interview.repository.InterviewResultRepository;
import com.allinone.DevView.user.entity.User;
import com.allinone.DevView.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.allinone.DevView.common.enums.InterviewType;
import com.allinone.DevView.interview.entity.Interview;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityPostsRepository postsRepository;
    private final UserRepository userRepository;
    private final InterviewResultRepository interviewResultRepository;

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
        if (post.getInterviewResultId() == null) throw new IllegalArgumentException("인터뷰 결과 ID가 필요합니다.");

        InterviewResult r = interviewResultRepository.findById(post.getInterviewResultId())
                .orElseThrow(() -> new IllegalArgumentException("인터뷰 결과를 찾을 수 없습니다. id=" + post.getInterviewResultId()));

        if (isBlank(post.getLevel())) {
            String mappedLevel = extractCareerLevelToPostLevel(r);
            if (!isBlank(mappedLevel)) post.setLevel(mappedLevel);
        }
        if (isBlank(post.getCategory())) {
            String mappedCategory = extractJobPositionToCategory(r);
            if (!isBlank(mappedCategory)) post.setCategory(mappedCategory);
        }

        return postsRepository.save(post);
    }

    public Long createInterviewSharePost(CreateInterviewSharePostRequest dto, Long userId) {
        if (dto == null) throw new IllegalArgumentException("인터뷰 공유 글 데이터가 비었습니다.");
        if (isBlank(dto.getTitle())) throw new IllegalArgumentException("제목을 입력해주세요.");
        if (dto.getInterviewResultId() == null) throw new IllegalArgumentException("인터뷰 결과 ID가 필요합니다.");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        InterviewResult r = interviewResultRepository.findById(dto.getInterviewResultId())
                .orElseThrow(() -> new IllegalArgumentException("인터뷰 결과를 찾을 수 없습니다. id=" + dto.getInterviewResultId()));

        CommunityPosts post = new CommunityPosts();
        post.setUser(user);
        post.setTitle(dto.getTitle());
        post.setContent(nvl(dto.getContent()));
        post.setInterviewResultId(dto.getInterviewResultId());
        post.setScore(r.getTotalScore());
        post.setGrade(r.getGrade());
        post.setInterviewFeedback(r.getFeedback());

        String mappedCategory = extractJobPositionToCategory(r);
        if (!isBlank(mappedCategory)) post.setCategory(mappedCategory);

        String mappedLevel = extractCareerLevelToPostLevel(r);
        if (!isBlank(mappedLevel)) post.setLevel(mappedLevel);

        postsRepository.save(post);
        return post.getPostId();
    }

    public Long createFreePost(CreatePostRequest dto, Long userId) {
        if (dto == null) throw new IllegalArgumentException("자유 글 데이터가 비었습니다.");
        if (isBlank(dto.title())) throw new IllegalArgumentException("제목을 입력해주세요.");

        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        CommunityPosts post = new CommunityPosts();
        post.setUser(user);
        post.setTitle(dto.title());
        post.setContent(nvl(dto.content()));
        postsRepository.save(post);
        return post.getPostId();
    }

    public CommunityPosts updatePost(Long id, CommunityPosts src) {
        CommunityPosts post = postsRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));
        if (!isBlank(src.getTitle())) post.setTitle(src.getTitle());
        if (!isBlank(src.getContent())) post.setContent(src.getContent());
        if (src.getInterviewResultId() != null && !src.getInterviewResultId().equals(post.getInterviewResultId())) {
            InterviewResult r = interviewResultRepository.findById(src.getInterviewResultId())
                    .orElseThrow(() -> new IllegalArgumentException("인터뷰 결과를 찾을 수 없습니다. id=" + src.getInterviewResultId()));
            post.setInterviewResultId(r.getId());
            post.setScore(r.getTotalScore());
            post.setGrade(r.getGrade());
            post.setInterviewFeedback(r.getFeedback());

            String mappedCategory = extractJobPositionToCategory(r);
            if (!isBlank(mappedCategory)) post.setCategory(mappedCategory);

            String mappedLevel = extractCareerLevelToPostLevel(r);
            if (!isBlank(mappedLevel)) post.setLevel(mappedLevel);
        }
        return postsRepository.save(post);
    }

    public Long updatePost(Long postId, Long userId, PostUpdateRequestDto req) {
        CommunityPosts post = postsRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));
        if (!post.getUser().getUserId().equals(userId)) throw new IllegalArgumentException("수정 권한이 없습니다.");

        if (!isBlank(req.getTitle())) post.setTitle(req.getTitle());
        if (!isBlank(req.getContent())) post.setContent(req.getContent());

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

    private static String interviewTypeLabel(InterviewType t) {
        if (t == null) return null;
        switch (t) {
            case TECHNICAL:     return "기술";
            case PRACTICAL:     return "실무";
            case BEHAVIORAL:    return "인성";
            case COMPREHENSIVE: return "종합";
            default:            return t.name();
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
        dto.setInterviewType(post.getInterviewType());
        dto.setInterviewTypeLabel(interviewTypeLabel(post.getInterviewType()));
        dto.setScore(post.getScore() != null ? post.getScore() : 0);
        dto.setGrade(post.getGrade() != null ? post.getGrade().name() : "--");
        dto.setViewCount(post.getViewCount());
        dto.setLikeCount(post.getLikeCount());
        dto.setScrapCount(post.getScrapCount());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setInterviewResultId(post.getInterviewResultId());
        dto.setInterviewFeedback(post.getInterviewFeedback());
        return dto;
    }

    private String extractCareerLevelToPostLevel(InterviewResult r) {
        if (r == null) return null;
        Interview iv = r.getInterview();
        if (iv == null) return null;
        Object career = iv.getCareerLevel();
        return normalizeCareerLevel(career);
    }

    private String normalizeCareerLevel(Object careerLevel) {
        if (careerLevel == null) return null;
        String s = careerLevel.toString().trim();
        if (isBlank(s)) return null;
        String key = s.toUpperCase();
        if ("MID".equals(key) || "MIDLEVEL".equals(key)) return "MID_LEVEL";
        if ("MID_LEVEL".equals(key)) return "MID_LEVEL";
        if ("JUNIOR".equals(key)) return "JUNIOR";
        if ("SENIOR".equals(key)) return "SENIOR";
        return key;
    }

    private String extractJobPositionToCategory(InterviewResult r) {
        if (r == null) return null;
        Interview iv = r.getInterview();
        if (iv == null) return null;
        Object job = iv.getJobPosition();
        return normalizeJobCategory(job);
    }

    private String normalizeJobCategory(Object jobPosition) {
        if (jobPosition == null) return null;
        String s = jobPosition.toString().trim();
        if (isBlank(s)) return null;
        String k = s.toUpperCase().replace('/', '_');
        if ("백엔드".equalsIgnoreCase(s)) return "BACKEND";
        if ("프론트엔드".equalsIgnoreCase(s)) return "FRONTEND";
        if ("풀스택".equalsIgnoreCase(s)) return "FULLSTACK";
        if ("데브옵스".equalsIgnoreCase(s)) return "DEVOPS";
        if ("DATA_AI".equals(k) || "DATA".equals(k) || "AI".equals(k)) return "DATA_AI";
        switch (k) {
            case "BACKEND":   return "BACKEND";
            case "FRONTEND":  return "FRONTEND";
            case "FULLSTACK": return "FULLSTACK";
            case "DEVOPS":    return "DEVOPS";
            default:          return "DATA_AI".equals(k) ? "DATA_AI" : k;
        }
    }
}
