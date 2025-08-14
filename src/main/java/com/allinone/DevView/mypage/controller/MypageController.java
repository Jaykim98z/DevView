package com.allinone.DevView.mypage.controller;

import com.allinone.DevView.common.exception.UserNotFoundException;
import com.allinone.DevView.mypage.dto.CareerChartDto;
import com.allinone.DevView.mypage.dto.MypageResponseDto;
import com.allinone.DevView.mypage.dto.ScoreGraphDto;
import com.allinone.DevView.mypage.service.MypageService;
import com.allinone.DevView.user.dto.response.UserResponse;
import com.allinone.DevView.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;  // 추가
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage")
@Slf4j  // 추가
public class MypageController {

    private final MypageService mypageService;
    private final UserService userService;

    @GetMapping
    public String showMyPage(Model model, HttpSession session) {
        UserResponse loginUser = (UserResponse) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/user/login";
        }

        Long userId = loginUser.getUserId();
        try {
            MypageResponseDto userInfo = mypageService.getMypageData(userId);
            model.addAttribute("user", userInfo);

            ScoreGraphDto scoreGraph = mypageService.getScoreGraphData(userId);
            if (scoreGraph == null) {
                scoreGraph = new ScoreGraphDto(List.of(), List.of());
            }
            model.addAttribute("scoreGraph", scoreGraph);

            CareerChartDto careerChart = mypageService.getCareerChartData(userId);
            if (careerChart == null) {
                careerChart = new CareerChartDto(List.of(), List.of());
            }
            model.addAttribute("careerChart", careerChart);

            return "mypage/mypage";
        } catch (UserNotFoundException e) {
            return "redirect:/user/login";
        }
    }

    /*** 마이페이지 수정 폼 뷰 (화면 라우팅 유지) */
    @GetMapping("/edit")
    public String editPage(Model model, HttpSession session) {
        UserResponse loginUser = (UserResponse) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/user/login";
        }
        model.addAttribute("user", mypageService.getBasicUserInfo(loginUser.getUserId()));
        return "mypage/mypage-edit";
    }

    /**
     * 회원탈퇴 처리
     * POST /mypage/delete
     */
    @PostMapping("/delete")
    public String deleteUser(HttpSession session) {
        log.info("회원탈퇴 요청");

        try {
            UserResponse loginUser = (UserResponse) session.getAttribute("loginUser");
            if (loginUser == null) {
                log.warn("로그인되지 않은 사용자의 탈퇴 시도");
                return "redirect:/user/login?error=authentication_required";
            }

            Long userId = loginUser.getUserId();
            String userEmail = loginUser.getEmail();

            // 추가 보안: 현재 세션의 사용자가 실제로 존재하는지 확인
            try {
                userService.getUserById(userId);
            } catch (Exception e) {
                log.warn("존재하지 않는 사용자의 탈퇴 시도: userId={}, email={}", userId, userEmail);
                session.invalidate();
                return "redirect:/user/login?error=user_not_found";
            }

            // 회원탈퇴 처리
            userService.deleteUser(userId);

            // 즉시 세션 무효화 (보안상 중요)
            session.invalidate();

            log.info("회원탈퇴 완료: userId={}, email={}", userId, userEmail);

            // 탈퇴 완료 후 메인 페이지로 리다이렉트
            return "redirect:/?message=withdrawal_success";

        } catch (Exception e) {
            log.error("회원탈퇴 처리 중 오류 발생", e);
            return "redirect:/mypage?error=withdrawal_failed";
        }
    }
}