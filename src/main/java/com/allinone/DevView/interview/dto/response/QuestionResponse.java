package com.allinone.DevView.interview.dto.response;

import com.allinone.DevView.interview.entity.InterviewQuestion;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO for responding with the details of a single interview question.
 */
@Getter
@Builder
public class QuestionResponse {
    private Long questionId;
    private String text;

    /**
     * Creates a QuestionResponse DTO from an InterviewQuestion entity.
     * @param question The entity to convert.
     * @return A new QuestionResponse instance.
     */
    public static QuestionResponse fromEntity(InterviewQuestion question) {
        return QuestionResponse.builder()
                .questionId(question.getId())
                .text(question.getText())
                .build();
    }
}
