package com.allinone.DevView.community.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ToggleResponse {
    private boolean active;
    private long count;
}
