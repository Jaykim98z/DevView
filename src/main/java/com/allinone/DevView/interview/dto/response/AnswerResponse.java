package com.allinone.DevView.interview.dto.response;

import com.allinone.DevView.interview.entity.InterviewAnswer;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO for responding with the result of a submitted answer.
 */
@Getter
@Builder
public class AnswerResponse {
    private Long answerId;
    private Long questionId;

    /**
     * Creates an AnswerResponse DTO from an InterviewAnswer entity.
     * @param answer The entity to convert.
     * @return A new AnswerResponse instance.
     */
    public static AnswerResponse fromEntity(InterviewAnswer answer) {
        return AnswerResponse.builder()
                .answerId(answer.getId())
                .questionId(answer.getQuestion().getId())
                .build();
    }
}
