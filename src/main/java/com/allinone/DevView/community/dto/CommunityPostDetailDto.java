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
    private Long writerId;
    private String content;
    private int score;
    private String grade;
    private int viewCount;
    private int likeCount;
    private int scrapCount;
    private LocalDateTime createdAt;
    private String interviewType;

    public static CommunityPostDetailDto from(CommunityPosts post) {
        if (post == null) {
            throw new IllegalArgumentException("post is null");
        }

        String username = (post.getUser() != null) ? post.getUser().getUsername() : null;

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
                .writerId(post.getUser() != null ? post.getUser().getUserId() : null)
                .content(post.getContent())
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
