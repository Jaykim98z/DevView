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

import java.lang.reflect.Method;
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

        assertMyResult(post.getInterviewResultId(), post.getUser().getUserId());

        return postsRepository.save(post);
    }

    public Long createInterviewSharePost(CreateInterviewSharePostRequest dto, Long userId) {
        if (dto == null) throw new IllegalArgumentException("인터뷰 공유 글 데이터가 비었습니다.");
        if (isBlank(dto.getTitle())) throw new IllegalArgumentException("제목을 입력해주세요.");
        if (dto.getInterviewResultId() == null) throw new IllegalArgumentException("인터뷰 결과 ID가 필요합니다.");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        assertMyResult(dto.getInterviewResultId(), userId);

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

        post.setType("INTERVIEW_SHARE");

        String rawCategory = coalesceFirstString(dto, "getCategoryCode", "getCategory", "getJobCategory", "getJobCategoryCode");
        String rawLevel    = coalesceFirstString(dto, "getLevelCode", "getLevel", "getCareerLevel", "getCareerLevelCode");
        String techTag     = coalesceFirstString(dto, "getTechTag", "techTag");
        String summary     = coalesceFirstString(dto, "getSummary", "summary");

        if (!isBlank(techTag)) post.setTechTag(techTag);
        if (!isBlank(summary)) post.setSummary(summary);

        String mappedCategory = mapCategory(rawCategory);
        String mappedLevel    = mapLevel(rawLevel);

        if (!isBlank(mappedCategory)) post.setCategory(mappedCategory);
        if (!isBlank(mappedLevel))    post.setLevel(mappedLevel);

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

        post.setType("POST");

        String rawCategory = coalesceFirstString(dto, "category", "categoryCode", "jobCategory", "jobCategoryCode");
        String rawLevel    = coalesceFirstString(dto, "level", "levelCode", "careerLevel", "careerLevelCode");
        String techTag     = coalesceFirstString(dto, "techTag", "getTechTag");
        String summary     = coalesceFirstString(dto, "summary", "getSummary");

        if (!isBlank(techTag)) post.setTechTag(techTag);
        if (!isBlank(summary)) post.setSummary(summary);

        String mappedCategory = mapCategory(rawCategory);
        String mappedLevel    = mapLevel(rawLevel);

        if (!isBlank(mappedCategory)) post.setCategory(mappedCategory);
        if (!isBlank(mappedLevel))    post.setLevel(mappedLevel);

        postsRepository.save(post);
        return post.getPostId();
    }

    public CommunityPosts updatePost(Long id, CommunityPosts src) {
        CommunityPosts post = postsRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));
        if (!isBlank(src.getTitle())) post.setTitle(src.getTitle());
        if (!isBlank(src.getContent())) post.setContent(src.getContent());
        if (src.getInterviewResultId() != null && !src.getInterviewResultId().equals(post.getInterviewResultId())) {

            assertMyResult(src.getInterviewResultId(), post.getUser().getUserId());

            InterviewResult r = interviewResultRepository.findById(src.getInterviewResultId())
                    .orElseThrow(() -> new IllegalArgumentException("인터뷰 결과를 찾을 수 없습니다. id=" + src.getInterviewResultId()));
            post.setInterviewResultId(r.getId());
            post.setScore(r.getTotalScore());
            post.setGrade(r.getGrade());
            post.setInterviewFeedback(r.getFeedback());
        }
        return postsRepository.save(post);
    }

    public Long updatePost(Long postId, Long userId, PostUpdateRequestDto req) {
        CommunityPosts post = postsRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("게시글 없습니다."));
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

    private void assertMyResult(Long resultId, Long userId) {
        if (resultId == null) return;
        boolean mine = interviewResultRepository.existsByIdAndInterview_User_UserId(resultId, userId);
        if (!mine) {
            throw new IllegalArgumentException("본인의 면접 결과만 선택할 수 있습니다.");
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

    private static String coalesceFirstString(Object target, String... methodNames) {
        if (target == null) return null;
        for (String name : methodNames) {
            try {
                Method m = target.getClass().getMethod(name);
                Object v = m.invoke(target);
                if (v != null) {
                    String s = String.valueOf(v);
                    if (!isBlank(s)) return s;
                }
            } catch (NoSuchMethodException ignored) {
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    private static String mapCategory(String raw) {
        if (isBlank(raw)) return null;
        String k = raw.trim().toLowerCase().replaceAll("\\s+", "");
        if (k.equals("백엔드") || k.equals("backend")) return "BACKEND";
        if (k.equals("프론트엔드") || k.equals("frontend")) return "FRONTEND";
        if (k.equals("풀스택") || k.equals("fullstack")) return "FULLSTACK";
        if (k.equals("devops")) return "DEVOPS";
        if (k.equals("data/ai") || k.equals("dataai") || k.equals("dataai ") || k.equals("dataai".trim()) || k.equals("dataai".replace(" ", "")) || k.equals("dataai".toLowerCase())) return "DATA_AI";
        if (raw.matches("^[A-Z_]+$")) return raw;
        return raw.toUpperCase();
    }

    private static String mapLevel(String raw) {
        if (isBlank(raw)) return null;
        String k = raw.trim();
        if (k.equals("주니어") || k.equalsIgnoreCase("junior")) return "JUNIOR";
        if (k.equals("미드레벨") || k.equalsIgnoreCase("mid") || k.equalsIgnoreCase("middle")) return "MID";
        if (k.equals("시니어") || k.equalsIgnoreCase("senior")) return "SENIOR";
        if (raw.matches("^[A-Z_]+$")) return raw;
        return raw.toUpperCase();
    }
}
