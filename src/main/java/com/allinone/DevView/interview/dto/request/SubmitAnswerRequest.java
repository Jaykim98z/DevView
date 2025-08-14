package com.allinone.DevView.interview.dto.request;

import lombok.Getter;

import java.util.List;

@Getter
public class SubmitAnswerRequest {
    private Long interviewId;
    private List<AnswerItem> answers;

    @Getter
    public static class AnswerItem {
        private Long questionId;
        private String answerText;
    }
}
