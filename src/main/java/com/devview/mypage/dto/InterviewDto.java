package com.devview.mypage.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InterviewDto {

    private final Long id;         // 상세보기용 인터뷰 ID
    private final String title;    // 면접 제목
    private final String date;     // yyyy.MM.dd 형식
    private final int score;       // 점수
    private final String grade;    // A / B / C / F

    /**
     * 등급에 따른 CSS 클래스 반환
     * 예: grade-a / grade-b / grade-c / grade-f
     */
    public String getGradeStyle() {
        return switch (grade) {
            case "A" -> "grade-a";
            case "B" -> "grade-b";
            case "C" -> "grade-c";
            default -> "grade-f";
        };
    }
}
