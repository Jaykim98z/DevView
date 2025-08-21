package com.allinone.DevView.community.dto;

import com.allinone.DevView.common.enums.Grade;
import java.time.LocalDateTime;

public record PostListDto(
        Long postId,
        String title,
        String writerName,
        String category,
        Grade grade,
        String level,
        String techTag,
        int likeCount,
        int scrapCount,
        int viewCount,
        int score,
        LocalDateTime createdAt
) {}

