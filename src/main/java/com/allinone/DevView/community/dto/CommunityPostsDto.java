package com.allinone.DevView.community.dto;

import com.allinone.DevView.common.enums.Grade;
import com.allinone.DevView.common.enums.InterviewType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class CommunityPostsDto {

    private Long id;
    private Long userId;

    private String username;
    private String profileImage;

    private String techTag;
    private String levelTag;

    private String title;
    private String summary;
    private String content;

    private InterviewType interviewType;
    private String interviewTypeLabel;

    private int score;
    private String grade;

    private int viewCount;
    private int likeCount;
    private int scrapCount;

    private boolean liked;
    private boolean bookmarked;
    private Long scrapId;

    private LocalDateTime createdAt;

    private int commentCount;

    public CommunityPostsDto(
            Long id,
            Long userId,
            String username,
            String techTag,
            String levelTag,
            String title,
            String summary,
            String content,
            InterviewType interviewType,
            String interviewTypeLabel,
            int score,
            String grade,
            int viewCount,
            int likeCount,
            int scrapCount,
            boolean liked,
            boolean bookmarked,
            Long scrapId,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.techTag = techTag;
        this.levelTag = levelTag;
        this.title = title;
        this.summary = summary;
        this.content = content;
        this.interviewType = interviewType;
        this.interviewTypeLabel = interviewTypeLabel;
        this.score = score;
        this.grade = grade;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.scrapCount = scrapCount;
        this.liked = liked;
        this.bookmarked = bookmarked;
        this.scrapId = scrapId;
        this.createdAt = createdAt;
    }

    public CommunityPostsDto(Long postId, Long userId, String username, String techTag, String levelTag, String title, String summary, String content, InterviewType interviewType, String interviewTypeLabel, int score, Grade grade, int viewCount, int likeCount, int scrapCount, boolean liked, boolean bookmarked, Object scrapId, LocalDateTime createdAt) {
    }
}
