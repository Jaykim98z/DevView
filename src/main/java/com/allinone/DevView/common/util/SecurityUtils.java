package com.allinone.DevView.common.util;

import com.allinone.DevView.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static Long getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("인증된 사용자가 없습니다.");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getUserId(); // CustomUserDetails에서 userId를 가져옴
        }

        throw new IllegalStateException("인증 객체가 CustomUserDetails가 아닙니다.");
    }
}
