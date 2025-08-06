package com.allinone.DevView.interview.entity;

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
    private LocalDateTime createdAt;

    private LocalDateTime endedAt;

    @Column(nullable = false)
    private int score; // 면접 점수

    @Column(nullable = true)
    private String feedback; // 면접 피드백

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public String getDate() {
        return createdAt.toLocalDate().toString(); // 날짜만 반환
    }

    public int getScore() {
        return score;
    }

    public String getInterviewType() {
        return interviewType.toString();
    }

    public String getFeedback() {
        return feedback;
    }

    // interviewDate를 반환하는 메서드 추가 (혹은 getDate()와 중복될 수 있음)
    public LocalDateTime getInterviewDate() {
        return this.createdAt;
    }
}
