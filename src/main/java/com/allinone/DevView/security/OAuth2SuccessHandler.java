package com.allinone.DevView.security;

import com.allinone.DevView.user.dto.response.UserResponse;
import com.allinone.DevView.user.service.OAuth2UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * OAuth2 로그인 성공 처리 핸들러
 * Google 로그인 성공 시 사용자 정보를 처리하고 세션에 저장
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final OAuth2UserService oAuth2UserService;

    // 세션에 저장할 사용자 정보 키
    private static final String LOGIN_USER = "loginUser";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        log.info("OAuth2 로그인 성공 처리 시작");

        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");
            String providerId = oAuth2User.getAttribute("sub"); // Google의 고유 ID

            log.info("OAuth2 사용자 정보 - email: {}, name: {}, providerId: {}", email, name, providerId);

            if (email == null || email.trim().isEmpty()) {
                log.error("OAuth2 로그인 실패 - 이메일 정보 없음");
                response.sendRedirect("/user/login?error=no_email");
                return;
            }

            // 사용자 정보 처리 및 세션 저장
            UserResponse userResponse = oAuth2UserService.handleOAuth2User(email, name, providerId);
            HttpSession session = request.getSession();
            session.setAttribute(LOGIN_USER, userResponse);

            log.info("OAuth2 로그인 성공 - 세션 저장 완료: userId={}, email={}",
                    userResponse.getUserId(), userResponse.getEmail());

            // 메인 페이지로 리다이렉트
            response.sendRedirect("/");

        } catch (Exception e) {
            log.error("OAuth2 로그인 처리 중 오류 발생", e);
            response.sendRedirect("/user/login?error=oauth2_process");
        }
    }
}
