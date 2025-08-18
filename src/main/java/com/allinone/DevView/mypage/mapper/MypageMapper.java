package com.allinone.DevView.mypage.mapper;

import com.allinone.DevView.mypage.dto.UserProfileUpdateRequest;
import com.allinone.DevView.mypage.entity.UserProfile;
import com.allinone.DevView.user.entity.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class MypageMapper {

    /**
     * User + UserProfile 엔티티에 프로필 수정 내용 반영
     *
     * @param user        수정 대상 사용자
     * @param userProfile 해당 사용자의 프로필 (null 아님)
     * @param req         수정 요청 DTO
     */
    public void applyProfileUpdates(User user, UserProfile userProfile, UserProfileUpdateRequest req) {
        if (req == null) {
            return;
        }

        // 1. User.username 업데이트
        updateIfTextPresent(req.getName(), user::setUsername);

        // 2. UserProfile 필드 업데이트
        if (userProfile != null) { // 안전 장치 (실제로는 null이 아니어야 함)
            updateIfTextPresent(req.getJob(), userProfile::setJob);
            updateIfTextPresent(req.getCareerLevel(), userProfile::setCareerLevel);

            // 3. 자기소개 업데이트 (null이나 빈 문자열도 허용하여 삭제 가능)
            updateStringField(req.getSelfIntroduction(), userProfile::setSelfIntroduction);
        }
    }

    /**
     * 문자열이 비어있지 않으면 setter 호출
     *
     * @param value  업데이트할 값
     * @param setter 적용할 setter 메서드
     */
    private void updateIfTextPresent(String value, java.util.function.Consumer<String> setter) {
        if (StringUtils.hasText(value)) {
            setter.accept(value);
        }
    }

    /**
     * 자기소개 같은 선택적 필드 업데이트 (null/빈값도 허용하여 삭제 가능)
     *
     * @param value  업데이트할 값 (null이면 업데이트하지 않음, 빈 문자열이면 삭제)
     * @param setter 적용할 setter 메서드
     */
    private void updateStringField(String value, java.util.function.Consumer<String> setter) {
        if (value != null) { // null이 아닌 경우만 업데이트 (빈 문자열 포함)
            setter.accept(value.trim().isEmpty() ? null : value.trim());
        }
    }
}