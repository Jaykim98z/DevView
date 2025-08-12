package com.allinone.DevView.community.dto;

import com.allinone.DevView.common.enums.Grade;
import jakarta.validation.constraints.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateInterviewSharePostRequest {

    @NotNull(message = "interviewResultId는 필수입니다.")
    private Long interviewResultId;

    @NotBlank(message = "title은 필수입니다.")
    @Size(max = 50, message = "title은 최대 50자입니다.")
    private String title;

    @NotBlank(message = "content는 필수입니다.")
    private String content;

    @NotNull(message = "grade는 필수입니다.")
    private Grade grade;

    @Min(value = 0, message = "score는 0 이상이어야 합니다.")
    @Max(value = 100, message = "score는 100 이하여야 합니다.")
    private int score;

    @NotBlank(message = "interviewFeedback은 필수입니다.")
    private String interviewFeedback;

}
