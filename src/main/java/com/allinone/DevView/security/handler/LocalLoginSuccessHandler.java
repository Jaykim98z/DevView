package com.allinone.DevView.security.handler;

import com.allinone.DevView.user.dto.response.UserResponse;
import com.allinone.DevView.user.entity.User;
import com.allinone.DevView.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 로컬 로그인 성공 처리 핸들러
 * 세션에 사용자 정보 저장 + JSON 응답
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LocalLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    private static final String LOGIN_USER = "loginUser";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        String email = authentication.getName();
        log.info("로컬 로그인 성공 처리 시작: email={}", email);

        try {
            // 사용자 정보 조회 및 세션 저장
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + email));

            UserResponse userResponse = UserResponse.from(user);
            HttpSession session = request.getSession();
            session.setAttribute(LOGIN_USER, userResponse);

            log.info("로컬 로그인 성공 - 세션 저장 완료: userId={}", userResponse.getUserId());

            // JSON 응답
            sendSuccessResponse(response, email, userResponse.getUsername());

        } catch (Exception e) {
            log.error("로컬 로그인 처리 중 오류: email={}", email, e);
            sendErrorResponse(response, "로그인 처리 중 오류가 발생했습니다.");
        }
    }

    private void sendSuccessResponse(HttpServletResponse response, String email, String username) throws IOException {
        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "로그인 성공");
        result.put("email", email);
        result.put("username", username);

        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(500);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);

        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}