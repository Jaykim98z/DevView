package com.allinone.DevView.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 로컬 로그인 실패 처리 핸들러
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LocalLoginFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        String email = request.getParameter("email");
        log.warn("로컬 로그인 실패: email={}, reason={}", email, exception.getMessage());

        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "이메일 또는 비밀번호가 올바르지 않습니다.");

        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}