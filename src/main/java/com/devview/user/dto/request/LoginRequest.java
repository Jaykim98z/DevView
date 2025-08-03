package com.devview.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 로그인 요청 DTO
 * 프론트엔드에서 로그인 폼 데이터를 받을 때 사용
 */
@Getter @Setter
public class LoginRequest {

    /**
     * 이메일
     * - 필수 입력 (공백 불가)
     * - 올바른 이메일 형식이어야 함
     */
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    /**
     * 비밀번호
     * - 필수 입력 (공백 불가)
     */
    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;
}