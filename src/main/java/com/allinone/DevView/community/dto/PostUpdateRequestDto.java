package com.allinone.DevView.community.dto;

import com.allinone.DevView.common.enums.Grade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostUpdateRequestDto {

    @NotBlank(message = "제목은 비어있을 수 없습니다.")
    @Size(max = 150, message = "제목은 150자 이하여야 합니다.")
    private String title;

    @NotBlank(message = "내용은 비어있을 수 없습니다.")
    private String content;

    private Grade grade;
}
