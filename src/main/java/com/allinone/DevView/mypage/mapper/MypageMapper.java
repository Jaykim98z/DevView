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
        if (userProfile != null) { // 안전 장치 (실제로는 null이 아니어야 함)
            updateIfTextPresent(req.getJob(), userProfile::setJob);
            updateIfTextPresent(req.getCareerLevel(), userProfile::setCareerLevel);
        }
    }

    private void updateIfTextPresent(String value, java.util.function.Consumer<String> setter) {
        if (StringUtils.hasText(value)) {
            setter.accept(value);
        }
    }
}
