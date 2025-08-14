package com.allinone.DevView.interview.dto.response;

import com.allinone.DevView.interview.entity.InterviewQuestion;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuestionResponse {
    private Long questionId;
    private String text;

    public static QuestionResponse fromEntity(InterviewQuestion question) {
        return QuestionResponse.builder()
                .questionId(question.getId())
                .text(question.getText())
                .build();
    }
}
