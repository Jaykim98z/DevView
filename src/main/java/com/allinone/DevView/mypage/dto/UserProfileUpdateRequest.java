package com.allinone.DevView.mypage.dto;

import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateRequest {
    @NotBlank
    private String name;
    private String job;
    private String careerLevel;

    // 필요하면 email도 허용 가능(매퍼 주석 참고)
    // private String email;
}
