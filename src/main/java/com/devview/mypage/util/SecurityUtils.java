package com.devview.common.util;

import java.security.Principal;

public class SecurityUtils {
    public static Long getUserId(Principal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("인증 정보 없음");
        }
        try {
            return Long.parseLong(principal.getName());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("잘못된 사용자 ID 형식입니다.");
        }
    }
}
