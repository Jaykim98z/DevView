package com.allinone.DevView.mypage.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileUpdateRequest {

    private String name;         // 사용자 이름
    private String job;          // 직군 (예: 백엔드, 프론트엔드)
    private String careerLevel;  // 경력레벨 (예: 주니어, 시니어)
}
