package com.allinone.DevView.mypage.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScrapDto {
    private final Long scrapId;
    private final String scrapTitle;
    private String link;
    private int likes;
    private int comments;
}
