package com.devview.mypage.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ScrapDto {

    private final String title;    // 게시물 제목
    private final String link;     // 외부 또는 내부 링크
    private final int likes;       // 좋아요 수
    private final int comments;    // 댓글 수
}
