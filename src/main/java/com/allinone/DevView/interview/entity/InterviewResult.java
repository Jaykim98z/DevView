package com.allinone.DevView.interview.entity;

import com.allinone.DevView.common.enums.Grade;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "interview_results")
@Getter
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
