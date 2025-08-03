package com.devview.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 회원가입 요청 DTO
 * 프론트엔드에서 회원가입 폼 데이터를 받을 때 사용
 */
@Getter @Setter
public class RegisterRequest {

    /**
     * 사용자명
     * - 필수 입력 (공백 불가)
     * - 2자 이상 20자 이하
     */
    @NotBlank(message = "사용자명은 필수입니다.")
    @Size(min = 2, max = 20, message = "사용자명은 2~20자 사이여야 합니다.")
    private String username;

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
     * - 8자 이상 20자 이하
     */
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8~20자 사이여야 합니다.")
    private String password;

    /**
     * 비밀번호 확인
     * - 필수 입력 (공백 불가)
     * - password와 일치해야 함
     */
    @NotBlank(message = "비밀번호 확인은 필수입니다.")
    private String passwordConfirm;

    /**
     * 비밀번호 일치 확인 메서드
     *
     * @return boolean - 비밀번호가 일치하면 true, 아니면 false
     */
    public boolean isPasswordMatched() {
        return password != null && password.equals(passwordConfirm);
    }
}