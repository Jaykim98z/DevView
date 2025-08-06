package com.allinone.DevView.community.dto;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostResponseDto {
    private Long id;
    private String title;
    private String writerName;
    private String summary;
    private String category;
    private String level;
    private String type;
    private int score;
    private String grade;
    private int viewCount;
    private int likeCount;
    private int scrapCount;
}
