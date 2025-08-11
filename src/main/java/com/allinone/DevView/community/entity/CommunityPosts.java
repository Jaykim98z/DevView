package com.allinone.DevView.community.entity;

import com.allinone.DevView.common.enums.Grade;
import com.allinone.DevView.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "community_posts")
@Getter
@Setter
@NoArgsConstructor
public class CommunityPosts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 50)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(length = 255)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Grade grade;

    @Column(length = 255)
    private String level;

    public String getLevelTag() {
        return this.level;
    }

    @Column(name = "tech_tag", length = 50)
    private String techTag;

    @Column(name = "like_count", nullable = false)
    private int likeCount;

    @Column(name = "scrap_count", nullable = false)
    private int scrapCount;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @Column(length = 1000)
    private String summary;

    @Column(name = "interview_type", length = 20, nullable = false)
    private String interviewType;

    @Column(name = "writer_name", length = 255)
    private String writerName;

    @Column(nullable = false)
    private int score;

    @Column(length = 255)
    private String type;

    @Column(name = "interview_result_id")
    private Long interviewResultId;

    @Column(name = "interview_feedback", columnDefinition = "TEXT")
    private String interviewFeedback;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
