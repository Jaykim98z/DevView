package com.allinone.DevView.interview.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 면접 질문 엔티티
 */
@Entity
@Table(name = "interview_questions")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_id", nullable = false)
    private Interview interview;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;

    @Column(nullable = false, length = 50)
    private String category;
}
