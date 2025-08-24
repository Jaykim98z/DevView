package com.allinone.DevView.interview.entity;

import com.allinone.DevView.common.enums.Grade;
import jakarta.persistence.*;
import lombok.*;

/**
 * 면접 결과 엔티티
 */
@Entity
@Table(name = "interview_results")
@Getter
@Setter // 비동기식 서비스에서 setRecommendedResource를 위해 사용
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_id", nullable = false, unique = true)
    private Interview interview;

    @Column(nullable = false)
    private Integer totalScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Grade grade;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String feedback;

    @Column(columnDefinition = "TEXT")
    private String recommendedResource;
}
