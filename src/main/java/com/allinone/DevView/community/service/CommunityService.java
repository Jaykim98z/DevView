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
        if (post.getInterviewResultId() == null) throw new IllegalArgumentException("인터뷰 결과 ID가 필요합니다.");
        return postsRepository.save(post);
    }

    public Long createInterviewSharePost(CreateInterviewSharePostRequest dto, Long userId) {
        if (dto == null) throw new IllegalArgumentException("인터뷰 공유 글 데이터가 비었습니다.");
        if (isBlank(dto.getTitle())) throw new IllegalArgumentException("제목을 입력해주세요.");
        if (dto.getInterviewResultId() == null) throw new IllegalArgumentException("인터뷰 결과 ID가 필요합니다.");

        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        CommunityPosts post = new CommunityPosts();
        post.setUser(user);
        post.setTitle(dto.getTitle());
        post.setContent(nvl(dto.getContent()));
        post.setInterviewResultId(dto.getInterviewResultId());

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
        if (src.getInterviewResultId() != null) post.setInterviewResultId(src.getInterviewResultId());
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
        dto.setTechTag(null);
        dto.setLevelTag(null);
        dto.setTitle(post.getTitle());
        dto.setSummary(null);
        dto.setContent(post.getContent());
        dto.setInterviewType(null);
        dto.setInterviewTypeLabel(null);
        dto.setScore(0);
        dto.setGrade(null);
        dto.setViewCount(post.getViewCount());
        dto.setLikeCount(post.getLikeCount());
        dto.setScrapCount(post.getScrapCount());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setInterviewResultId(post.getInterviewResultId());
        dto.setInterviewFeedback(null);
        return dto;
    }
}
