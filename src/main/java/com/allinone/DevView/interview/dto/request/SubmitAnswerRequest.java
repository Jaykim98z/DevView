package com.allinone.DevView.interview.dto.request;

import lombok.Getter;

@Getter
public class SubmitAnswerRequest {
    private Long questionId;
    private String answerText;
}
