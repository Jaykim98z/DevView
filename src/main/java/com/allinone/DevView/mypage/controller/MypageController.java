package com.allinone.DevView.mypage.controller;

import com.allinone.DevView.common.exception.UserNotFoundException;
import com.allinone.DevView.mypage.dto.CareerChartDto;
import com.allinone.DevView.mypage.dto.MypageResponseDto;
import com.allinone.DevView.mypage.dto.ScoreGraphDto;
import com.allinone.DevView.mypage.service.MypageService;
import com.allinone.DevView.user.dto.response.UserResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MypageController {

    private final MypageService mypageService;

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
}
