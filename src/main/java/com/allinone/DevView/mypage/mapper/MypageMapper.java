package com.allinone.DevView.mypage.mapper;

import com.allinone.DevView.mypage.dto.UserProfileUpdateRequest;
import com.allinone.DevView.mypage.entity.UserProfile;
import com.allinone.DevView.user.entity.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class MypageMapper {


    public void applyProfileUpdates(User user, UserProfile userProfile, UserProfileUpdateRequest req) {
        if (req == null) {
            return;
        }

        // 1. User.username 업데이트
        updateIfTextPresent(req.getName(), user::setUsername);

        // 2. UserProfile 필드 업데이트
        if (userProfile != null) {
            updateIfTextPresent(req.getJob(), userProfile::setJob);
            updateIfTextPresent(req.getCareerLevel(), userProfile::setCareerLevel);

            // 3. 자기소개 업데이트
            updateStringField(req.getSelfIntroduction(), userProfile::setSelfIntroduction);
        }
    }

    private void updateIfTextPresent(String value, java.util.function.Consumer<String> setter) {
        if (StringUtils.hasText(value)) {
            setter.accept(value);
        }
    }

    /**
     * 자기소개 같은 선택적 필드 업데이트 (null/빈값도 허용하여 삭제 가능)
     *
     * @param value  업데이트할 값
     * @param setter 적용할 setter 메서드
     */
    private void updateStringField(String value, java.util.function.Consumer<String> setter) {
        if (value != null) { // null이 아닌 경우만 업데이트 (빈 문자열 포함)
            setter.accept(value.trim().isEmpty() ? null : value.trim());
        }
    }
}