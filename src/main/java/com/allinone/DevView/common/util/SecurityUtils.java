package com.allinone.DevView.common.util;

import java.security.Principal;

public class SecurityUtils {

    public static Long getUserId(Principal principal) {
        // principal.getName()이 사용자 ID를 포함한다고 가정
        return Long.parseLong(principal.getName());
    }
}