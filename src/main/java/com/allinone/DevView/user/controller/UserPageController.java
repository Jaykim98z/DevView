package com.allinone.DevView.user.controller;

import com.allinone.DevView.user.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;

/**
 * 사용자 인증 관련 페이지 컨트롤러 (세션 기반)
 * 로그인, 회원가입, 로그아웃 기능만 담당
 */
@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UserPageController {

    private static final String LOGIN_USER = "loginUser";

    /**
     * 로그인 페이지 렌더링
     */
    @GetMapping("/login")
    public String loginPage(HttpSession session, Model model) {
        log.info("로그인 페이지 요청");

        UserResponse loginUser = getLoginUser(session);
        if (loginUser != null) {
            log.info("이미 로그인된 사용자 - 메인 페이지로 리다이렉트: userId={}", loginUser.getUserId());
            return "redirect:/";
        }

        model.addAttribute("pageTitle", "로그인");
        model.addAttribute("isLoginPage", true);
        return "login/login";
    }

    /**
     * 회원가입 페이지 렌더링
     */
    @GetMapping("/register")
    public String registerPage(HttpSession session, Model model) {
        log.info("회원가입 페이지 요청");

        UserResponse loginUser = getLoginUser(session);
        if (loginUser != null) {
            log.info("이미 로그인된 사용자 - 메인 페이지로 리다이렉트: userId={}", loginUser.getUserId());
            return "redirect:/";
        }

        model.addAttribute("pageTitle", "회원가입");
        model.addAttribute("isRegisterPage", true);
        return "register/register";
    }

    /**
     * 로그아웃 처리 (POST 방식)
     */
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        log.info("로그아웃 요청");

        try {
            UserResponse loginUser = getLoginUser(session);
            if (loginUser != null) {
                log.info("로그아웃 처리: userId={}", loginUser.getUserId());
            }

            // 세션 무효화 (로그인 정보 삭제)
            session.invalidate();
            log.info("로그아웃 성공 - 세션 무효화 완료");

        } catch (Exception e) {
            log.warn("로그아웃 중 오류 발생", e);
        }

        return "redirect:/";
    }

    /**
     * 세션에서 로그인 사용자 정보 가져오기
     */
    private UserResponse getLoginUser(HttpSession session) {
        return (UserResponse) session.getAttribute(LOGIN_USER);
    }

    /**
     * 로컬 로그인 처리 (POST 방식)
     */
    @PostMapping("/login")
    public String login(HttpSession session, String email, String password) {
        log.info("로그인 요청: email={}", email);

        UserResponse user = UserResponse.authenticateUser(email, password);
        if (user != null) {
            session.setAttribute(LOGIN_USER, user);
            log.info("로그인 성공: userId={}", user.getUserId());

            return "redirect:/";
        } else {
            log.warn("로그인 실패: 잘못된 사용자 정보");
            return "redirect:/user/login?error=true";
        }
    }
}
