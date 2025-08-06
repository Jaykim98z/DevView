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

    public static CommunityPostDetailDto from(CommunityPosts post) {
        return CommunityPostDetailDto.builder()
                .id(post.getPostId())
                .title(post.getTitle())
                .summary(post.getContent().length() > 100
                        ? post.getContent().substring(0, 100) + "..."
                        : post.getContent())
                .username(post.getUser().getUsername())
                .score(post.getScore())
                .grade(post.getGrade().name())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .scrapCount(post.getScrapCount())
                .createdAt(post.getCreatedAt())
                .interviewType(post.getInterviewType())
                .build();
    }
}
