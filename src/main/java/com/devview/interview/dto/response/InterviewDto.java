package com.devview.interview.dto.response;

public class InterviewDto {
    private Long id;
    private String title;
    private String date;
    private int score;
    private String grade;

    public String getGradeStyle() {
        return switch (grade) {
            case "A" -> "grade-a";
            case "B" -> "grade-b";
            case "C" -> "grade-c";
            default -> "grade-f";
        };
    }
}
