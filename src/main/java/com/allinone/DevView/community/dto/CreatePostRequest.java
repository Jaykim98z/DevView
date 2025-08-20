package com.allinone.DevView.community.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequest {

    @NotBlank
    @Size(max = 50)
    private String title;

    @NotBlank
    private String content;

    @NotBlank
    private String interviewType;

    @NotBlank
    private String grade;

    @Size(max = 50)
    private String techTag;

    @Size(max = 255)
    private String level;

    @Size(max = 255)
    private String category;

    @Size(max = 255)
    private String type;

    @Min(0) @Max(100)
    private Integer score;
}
