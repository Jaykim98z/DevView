package com.allinone.DevView.interview.entity;

import com.allinone.DevView.common.enums.CareerLevel;
import com.allinone.DevView.common.enums.InterviewType;
import com.allinone.DevView.common.enums.JobPosition;
import com.allinone.DevView.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 면접 정보 엔티티
 */
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private JobPosition jobPosition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CareerLevel careerLevel;

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

    /**
     * 면접 세션 종료
     */
    public void endInterviewSession() {
        if (this.endedAt == null) {
            this.endedAt = LocalDateTime.now();
        }
    }
}
