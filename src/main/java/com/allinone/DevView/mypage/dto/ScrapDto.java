package com.allinone.DevView.mypage.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScrapDto {
    private String title;
    private String link;
    private int likes;
    private int comments;
}
