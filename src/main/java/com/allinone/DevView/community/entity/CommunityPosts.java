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

    @Column(name = "title", length = 50)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "category")
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "grade", length = 20, nullable = false)
    private Grade grade;

    @Column(name = "level")
    private String level;

    @Column(name = "like_count", nullable = false)
    private int likeCount;

    @Column(name = "scrap_count", nullable = false)
    private int scrapCount;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @Column(name = "summary", length = 1000)
    private String summary;

    @Column(name = "interview_type", length = 20, nullable = false)
    private String interviewType;

    @Column(name = "writer_name")
    private String writerName;

    @Column(name = "score", nullable = false)
    private int score;

    @Column(name = "type")
    private String type;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
