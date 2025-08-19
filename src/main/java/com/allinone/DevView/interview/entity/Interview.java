package com.allinone.DevView.interview.entity;

import com.allinone.DevView.common.enums.InterviewType;
import com.allinone.DevView.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "interviews")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interview_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "interview", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InterviewQuestion> questions = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InterviewType interviewType;

    @Column(nullable = false, length = 50)
    private String jobPosition;

    @Column(nullable = false, length = 50)
    private String careerLevel;

    @Column(nullable = false)
    private int questionCount;

    @Column(nullable = false)
    private int durationMinutes;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime endedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void endInterviewSession() {
        if (this.endedAt == null) {
            this.endedAt = LocalDateTime.now();
        }
    }
}
