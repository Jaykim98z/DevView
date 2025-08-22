package com.allinone.DevView.interview.dto.request;

import lombok.Getter;

import java.util.List;

/**
 * DTO for submitting all user answers for a completed interview.
 */
@Getter
public class SubmitAnswerRequest {
    private Long interviewId;
    private List<AnswerItem> answers;

    /**
     * Represents a single answer to a specific question.
     */
    @Getter
    public static class AnswerItem {
        private Long questionId;
        private String answerText;
    }
}
