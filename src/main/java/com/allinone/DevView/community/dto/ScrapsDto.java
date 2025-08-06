package com.allinone.DevView.community.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScrapsDto {

    private Long scrapId;

    private Long postId;

    private Long userId;
}
