package com.allinone.DevView.community.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentsDto {

    private Long id;

    private Long userId;

    private Long postId;

    private String content;

    private LocalDateTime createdAt;
}
