package com.allinone.DevView.community.entity;

import com.allinone.DevView.common.enums.Grade;
import com.allinone.DevView.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "community_posts",
        indexes = {
                @Index(name = "idx_posts_user", columnList = "user_id"),
                @Index(name = "idx_posts_created_at", columnList = "created_at"),
                @Index(name = "idx_posts_category", columnList = "category"),
                @Index(name = "idx_posts_grade", columnList = "grade"),
                @Index(name = "idx_posts_interview_type", columnList = "interview_type"),
                @Index(name = "idx_posts_deleted", columnList = "deleted")
        }
)
@Getter
@Setter
@NoArgsConstructor
@SQLDelete(sql = "UPDATE community_posts SET deleted = true WHERE post_id = ?")
@Where(clause = "deleted = false")
public class CommunityPosts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 50, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
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
    private int likeCount = 0;

    @Column(name = "scrap_count", nullable = false)
    private int scrapCount = 0;

    @Column(name = "view_count", nullable = false)
    private int viewCount = 0;

    @Column(length = 1000)
    private String summary;

    @Column(name = "interview_type", length = 20, nullable = false)
    private String interviewType;

    @Column(name = "writer_name", length = 255)
    private String writerName;

    @Column(nullable = false)
    private int score = 0;

    @Column(length = 255)
    private String type;

    @Column(name = "interview_result_id")
    private Long interviewResultId;

    @Column(name = "interview_feedback", columnDefinition = "TEXT")
    private String interviewFeedback;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }

        this.likeCount = this.likeCount;
        this.scrapCount = this.scrapCount;
        this.viewCount = this.viewCount;
        this.score = this.score;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void softDelete() {
        this.deleted = true;
    }
}
