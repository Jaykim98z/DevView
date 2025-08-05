package com.allinone.DevView.community.dto;

import com.allinone.DevView.common.enums.Grade;
import com.allinone.DevView.common.enums.InterviewType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommunityPostsDto {

    private Long id;

    private Long userId;

    private String title;

    private String content;

    private Grade grade;

    private int score;

    private int viewCount;

    private int likeCount;

    private int scrapCount;

    private InterviewType interviewType;

    private LocalDateTime createdAt;
}
