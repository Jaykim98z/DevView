package com.allinone.DevView.interview.dto.response;

import com.allinone.DevView.interview.entity.InterviewAnswer;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AnswerResponse {
    private Long answerId;
    private Long questionId;

    public static AnswerResponse fromEntity(InterviewAnswer answer) {
        return AnswerResponse.builder()
                .answerId(answer.getId())
                .questionId(answer.getQuestion().getId())
                .build();
    }
}
