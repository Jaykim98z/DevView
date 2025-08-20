package com.allinone.DevView.community.dto;

import com.allinone.DevView.common.enums.Grade;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateInterviewSharePostRequest {

    @NotNull
    private Long interviewResultId;

    private Grade grade;

    @Min(value = 0, message = "score는 0 이상이어야 합니다.")
    @Max(value = 100, message = "score는 100 이하여야 합니다.")
    private Integer score;

    @NotBlank
    private String title;

    @NotBlank
    private String interviewFeedback;

    private String content;
}
