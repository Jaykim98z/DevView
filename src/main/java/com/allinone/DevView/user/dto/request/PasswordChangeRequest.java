package com.allinone.DevView.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 비밀번호 변경 요청 DTO
 * 로그인된 사용자가 비밀번호를 변경할 때 사용
 */
@Getter @Setter
public class PasswordChangeRequest {

    /**
     * 현재 비밀번호
     * - 필수 입력 (공백 불가)
     * - 보안상 현재 비밀번호 확인 필요
     */
    @NotBlank(message = "현재 비밀번호는 필수입니다.")
    private String currentPassword;

    /**
     * 새 비밀번호
     * - 필수 입력 (공백 불가)
     * - 8자 이상 20자 이하
     */
    @NotBlank(message = "새 비밀번호는 필수입니다.")
    @Size(min = 8, max = 20, message = "새 비밀번호는 8~20자 사이여야 합니다.")
    private String newPassword;

    /**
     * 새 비밀번호 확인
     * - 필수 입력 (공백 불가)
     * - newPassword와 일치해야 함
     */
    @NotBlank(message = "새 비밀번호 확인은 필수입니다.")
    private String newPasswordConfirm;

    /**
     * 새 비밀번호 일치 확인 메서드
     *
     * @return boolean - 새 비밀번호가 일치하면 true, 아니면 false
     */
    public boolean isNewPasswordMatched() {
        return newPassword != null && newPassword.equals(newPasswordConfirm);
    }

    /**
     * 현재 비밀번호와 새 비밀번호가 같은지 확인
     *
     * @return boolean - 같으면 true, 다르면 false
     */
    public boolean isSameAsCurrentPassword() {
        return currentPassword != null && currentPassword.equals(newPassword);
    }
}