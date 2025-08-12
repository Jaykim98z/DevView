package com.allinone.DevView.community.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "scraps",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_scraps_post_user",
                columnNames = {"post_id", "user_id"}
        ),
        indexes = {
                @Index(name = "idx_scraps_post_id", columnList = "post_id"),
                @Index(name = "idx_scraps_user_id", columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Scraps {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scrap_id")
    private Long scrapId;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
