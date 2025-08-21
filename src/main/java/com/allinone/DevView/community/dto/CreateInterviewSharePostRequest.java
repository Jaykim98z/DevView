package com.allinone.DevView.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateInterviewSharePostRequest {

    @NotNull
    private Long interviewResultId;

    @NotBlank
    private String title;

    private String content;
}
