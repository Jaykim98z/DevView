package com.allinone.DevView.community.dto;

import com.allinone.DevView.common.enums.Grade;
import com.allinone.DevView.common.enums.InterviewType;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CreateInterviewSharePostRequest {

    @NotBlank
    @Size(max = 100)
    private String title;

    @NotBlank
    private String content;

    @NotNull
    private InterviewType interviewType;

    private Grade grade;


    private Integer score;
    private String interviewFeedback;
    private Long interviewResultId;


    @JsonSetter("interviewType")
    public void setInterviewTypeRaw(String v) {
        if (v == null) { this.interviewType = null; return; }
        String s = v.trim().toUpperCase();
        if ("PRACTICE".equals(s)) s = "PRACTICAL"; // 이넘은 수정하지 않고 alias 처리
        this.interviewType = InterviewType.valueOf(s);
    }

    @JsonSetter("grade")
    public void setGradeRaw(String v) {
        if (v == null || v.isBlank()) { this.grade = null; return; }
        this.grade = Grade.valueOf(v.trim().toUpperCase()); // "c" → C
    }

    @JsonSetter("score")
    public void setScoreRaw(Object v) {
        if (v == null) { this.score = null; return; }
        try {
            if (v instanceof Number n) { this.score = n.intValue(); return; }
            String s = v.toString().trim();
            if (s.isEmpty()) { this.score = null; return; }
            this.score = Integer.parseInt(s);
        } catch (Exception ignored) {
            this.score = null;
        }
    }
}
