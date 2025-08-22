package com.allinone.DevView.mypage.dto;

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
public class InterviewDto {

    private final Long interviewId;
    private final String interviewDate;
    private final InterviewType interviewType;
    private final int score;
    private final Grade grade;
    private final String feedback;

    private final String title;
    private final String detailUrl;

    private final JobPosition jobPosition;

    public String getJobPositionDisplayName() {
        return jobPosition != null ? jobPosition.getDisplayName() : "기타";
    }

    private final CareerLevel careerLevel;

    public String getCareerLevelDisplayName() {
        return careerLevel != null ? careerLevel.getDisplayName() : "-";
    }

    public static InterviewDto fromEntity(InterviewResult result) {
        Interview i = result.getInterview();
        LocalDateTime when = (i.getEndedAt() != null ? i.getEndedAt() : i.getCreatedAt());

        Grade commonGrade = result.getGrade();
        InterviewType commonType = i.getInterviewType();

        JobPosition jobEnum = JobPosition.fromString(i.getJobPosition());
        String jobDisplay = (jobEnum != null) ? jobEnum.getDisplayName() : "기타";

        CareerLevel levelEnum = CareerLevel.fromString(i.getCareerLevel());

        return InterviewDto.builder()
                .interviewId(i.getId())
                .interviewDate(when.toLocalDate().toString())
                .interviewType(commonType)
                .score(result.getTotalScore())
                .grade(commonGrade)
                .feedback(result.getFeedback())
                .title(jobDisplay + " 면접")
                .detailUrl("/interview/result/" + i.getId())
                .jobPosition(jobEnum)
                .careerLevel(levelEnum)
                .build();
    }
}
