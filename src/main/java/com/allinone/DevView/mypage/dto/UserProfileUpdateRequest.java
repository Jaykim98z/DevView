package com.allinone.DevView.mypage.dto;

import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    //자기소개
    @Size(max = 500, message = "자기소개는 500자 이내로 작성해주세요.")
    private String selfIntroduction;
}