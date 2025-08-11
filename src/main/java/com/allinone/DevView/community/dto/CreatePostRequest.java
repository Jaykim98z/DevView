package com.allinone.DevView.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePostRequest(
        @NotBlank @Size(max = 50) String title,
        @NotBlank String content,
        @NotBlank String interviewType,
        @NotBlank String grade,
        @Size(max = 50)  String techTag,
        @Size(max = 255) String level,
        @Size(max = 255) String category,
        @Size(max = 255) String type,

        Integer score  // optional
) {}