package com.allinone.DevView.mypage.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ScrapDto {
    private final String title;
    private final String link;
    private final int likes;
    private final int comments;
}
