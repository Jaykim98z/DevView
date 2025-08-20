package com.allinone.DevView.interview.dto.response;

import com.allinone.DevView.common.enums.CareerLevel;
import com.allinone.DevView.common.enums.Grade;
import com.allinone.DevView.common.enums.InterviewType;
import com.allinone.DevView.common.enums.JobPosition;
import com.allinone.DevView.interview.entity.Interview;
import com.allinone.DevView.interview.entity.InterviewResult;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class InterviewResultResponse {

    // 기존 필드
    private final Long resultId;
    private final Long interviewId;
    private final int totalScore;
    private final Grade grade;
    private final String feedback;
    private final String recommendedResource;

    // ✅ 마이페이지 표시용으로 추가 (기존 mypage.dto.InterviewDto 대체)
    private final String interviewDate;       // YYYY-MM-DD (템플릿에서 '.' 치환)
    private final InterviewType interviewType;
    private final JobPosition jobPosition;
    private final CareerLevel careerLevel;
    private final String title;
    private final String detailUrl;

    // ✅ 표시용 헬퍼 (템플릿에서 사용)
    public String getJobPositionDisplayName() {
        return jobPosition != null ? jobPosition.getDisplayName() : "기타";
    }

    public String getCareerLevelDisplayName() {
        return careerLevel != null ? careerLevel.getDisplayName() : "-";
    }

    public static InterviewResultResponse fromEntity(InterviewResult result) {
        Interview i = result.getInterview();

        // 날짜: endedAt 우선, 없으면 createdAt
        LocalDateTime when = (i.getEndedAt() != null ? i.getEndedAt() : i.getCreatedAt());

        // 제목/상세 URL
        String title = (i.getJobPosition() != null ? i.getJobPosition().getDisplayName() : "기타") + " 면접";
        String detailUrl = "/interview/result/" + i.getId();

        return InterviewResultResponse.builder()
                .resultId(result.getId())
                .interviewId(i.getId())
                .totalScore(result.getTotalScore())
                .grade(result.getGrade())
                .feedback(result.getFeedback())
                .recommendedResource(result.getRecommendedResource())
                .interviewDate(when != null ? when.toLocalDate().toString() : null)
                .interviewType(i.getInterviewType())
                .jobPosition(i.getJobPosition())
                .careerLevel(i.getCareerLevel())
                .title(title)
                .detailUrl(detailUrl)
                .build();
    }
}
