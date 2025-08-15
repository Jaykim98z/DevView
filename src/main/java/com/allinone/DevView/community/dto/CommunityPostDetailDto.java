package com.allinone.DevView.community.dto;

import com.allinone.DevView.community.entity.CommunityPosts;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommunityPostDetailDto {
    private Long id;
    private String title;
    private String summary;
    private String username;
    private int score;
    private String grade;
    private int viewCount;
    private int likeCount;
    private int scrapCount;
    private LocalDateTime createdAt;
    private String interviewType;

    /** 엔티티 -> DTO 매핑 (summary 필드 우선, 없으면 content 기준으로 100자 컷) */
    public static CommunityPostDetailDto from(CommunityPosts post) {
        if (post == null) {
            throw new IllegalArgumentException("post is null");
        }

        // username은 Lazy 로딩 주의: 레포에서 JOIN FETCH 하거나 서비스 @Transactional 범위 내에서 호출하세요.
        String username = (post.getUser() != null) ? post.getUser().getUsername() : null;

        // summary 우선 사용, 없으면 content 100자 컷(널 방어)
        String summary = post.getSummary();
        if (isBlank(summary)) {
            String content = post.getContent();
            if (!isBlank(content)) {
                summary = abbreviate(content, 100);
            } else {
                summary = "";
            }
        }

        return CommunityPostDetailDto.builder()
                .id(post.getPostId())
                .title(post.getTitle())
                .summary(summary)
                .username(username)
                .score(post.getScore())
                .grade(post.getGrade() != null ? post.getGrade().name() : null)
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .scrapCount(post.getScrapCount())
                .createdAt(post.getCreatedAt())
                .interviewType(post.getInterviewType())
                .build();
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String abbreviate(String text, int max) {
        if (text == null) return "";
        if (max < 3) return text.length() <= max ? text : text.substring(0, max);
        return text.length() <= max ? text : text.substring(0, max - 3) + "...";
    }
}
