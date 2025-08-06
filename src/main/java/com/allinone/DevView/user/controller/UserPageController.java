package com.allinone.DevView.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;

/**
 * 사용자 인증 관련 페이지 컨트롤러
 * 로그인, 회원가입, 로그아웃 페이지 렌더링 담당
 */
@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UserPageController {

    /**
     * 메인 페이지
     *
     * @param model 뷰에 전달할 데이터
     * @return 메인 페이지 템플릿
     */
    @GetMapping("/")
    public String mainPage(Model model) {
        log.info("메인 페이지 요청");

        // 메인 페이지에 필요한 데이터가 있다면 여기서 추가
        model.addAttribute("pageTitle", "AI 면접 시뮬레이터");

        return "main/index";
    }

    /**
     * 로그인 페이지 렌더링
     *
     * @param model 뷰에 전달할 데이터
     * @return 로그인 페이지 템플릿
     */
    @GetMapping("/login")
    public String loginPage(Model model) {
        log.info("로그인 페이지 요청");

        // 로그인 페이지에 필요한 데이터 추가
        model.addAttribute("pageTitle", "로그인");
        model.addAttribute("isLoginPage", true);

        return "login/login";
    }

    /**
     * 회원가입 페이지 렌더링
     *
     * @param model 뷰에 전달할 데이터
     * @return 회원가입 페이지 템플릿
     */
    @GetMapping("/register")
    public String registerPage(Model model) {
        log.info("회원가입 페이지 요청");

        // 회원가입 페이지에 필요한 데이터 추가
        model.addAttribute("pageTitle", "회원가입");
        model.addAttribute("isRegisterPage", true);

        return "register/register";
    }

    /**
     * 로그아웃 처리
     * 세션을 정리하고 메인 페이지로 리다이렉트
     *
     * @param session HTTP 세션
     * @return 메인 페이지로 리다이렉트
     */
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        log.info("로그아웃 요청");

        try {
            // 세션 무효화 (로그인 정보 삭제)
            session.invalidate();
            log.info("로그아웃 성공 - 세션 무효화 완료");

        } catch (Exception e) {
            log.warn("로그아웃 중 오류 발생", e);
        }

        // 메인 페이지로 리다이렉트
        return "redirect:/user/";
    }

//    /**
//     * 로그인 성공 후 호출되는 페이지
//     * (추후 세션/JWT 구현 시 사용)
//     *
//     * @param model 뷰에 전달할 데이터
//     * @return Dashboard 또는 메인 페이지
//     */
//    @GetMapping("/dashboard")
//    public String dashboard(Model model) {
//        log.info("대시보드 페이지 요청");
//
//        // TODO: 로그인 상태 확인 로직 추가 필요
//        // 로그인되지 않은 사용자라면 로그인 페이지로 리다이렉트
//
//        model.addAttribute("pageTitle", "대시보드");
//        model.addAttribute("isDashboard", true);
//
//        return "main/index"; // 현재는 메인 페이지로, 추후 대시보드 템플릿 생성 시 변경
//    }
}