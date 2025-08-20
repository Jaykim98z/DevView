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
 * ✅ 개선사항: LOCAL 사용자만 처리하여 GOOGLE 사용자와 완전 분리
 * - 세션에 사용자 정보 저장 + JSON 응답
 * - GOOGLE 사용자의 잘못된 로그인 시도 차단
 * - 명확한 로깅 및 에러 처리
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
            // LOCAL 사용자만 조회 (GOOGLE 사용자 제외)
            User user = userRepository.findLocalUserByEmail(email)
                    .orElseThrow(() -> {
                        log.error("로그인 성공 후 LOCAL 사용자를 찾을 수 없음: email={}", email);
                        return new RuntimeException("LOCAL 사용자를 찾을 수 없습니다: " + email);
                    });

            // 이중 체크: GOOGLE 사용자가 잘못 로그인한 경우 방지
            if (user.isGoogleUser()) {
                log.error("GOOGLE 사용자가 로컬 로그인에 성공함 (보안 이슈): email={}, provider={}",
                        email, user.getProvider());
                sendErrorResponse(response, "소셜 로그인을 이용해주세요.");
                return;
            }

            // 비밀번호가 없는 경우 체크 (OAuth2 사용자)
            if (user.getPassword() == null) {
                log.error("비밀번호가 없는 사용자가 로컬 로그인에 성공함 (보안 이슈): email={}, provider={}",
                        email, user.getProvider());
                sendErrorResponse(response, "소셜 로그인을 이용해주세요.");
                return;
            }

            // UserResponse 생성 및 세션 저장
            UserResponse userResponse = UserResponse.from(user);
            HttpSession session = request.getSession();
            session.setAttribute(LOGIN_USER, userResponse);

            log.info("로컬 로그인 성공 - 세션 저장 완료: userId={}, email={}, provider={}",
                    userResponse.getUserId(), userResponse.getEmail(), userResponse.getProvider());

            // 성공 JSON 응답
            sendSuccessResponse(response, email, userResponse.getUsername());

        } catch (Exception e) {
            log.error("로컬 로그인 처리 중 오류: email={}", email, e);
            sendErrorResponse(response, "로그인 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 로그인 성공 JSON 응답
     *
     * @param response HTTP 응답
     * @param email 로그인한 이메일
     * @param username 사용자명
     * @throws IOException JSON 응답 작성 실패 시
     */
    private void sendSuccessResponse(HttpServletResponse response, String email, String username) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "로그인이 완료되었습니다.");
        result.put("email", email);
        result.put("username", username);
        result.put("provider", "LOCAL");

        String jsonResponse = objectMapper.writeValueAsString(result);
        response.getWriter().write(jsonResponse);

        log.debug("로그인 성공 응답 전송 완료: email={}", email);
    }

    /**
     * 로그인 실패 JSON 응답
     *
     * @param response HTTP 응답
     * @param message 에러 메시지
     * @throws IOException JSON 응답 작성 실패 시
     */
    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);

        String jsonResponse = objectMapper.writeValueAsString(result);
        response.getWriter().write(jsonResponse);

        log.warn("로그인 실패 응답 전송: message={}", message);
    }
}