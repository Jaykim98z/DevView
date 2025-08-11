package com.allinone.DevView.mypage.controller;

import com.allinone.DevView.common.util.SecurityUtils;
import com.allinone.DevView.mypage.dto.CareerChartDto;
import com.allinone.DevView.mypage.dto.MypageResponseDto;
import com.allinone.DevView.mypage.dto.ScoreGraphDto;
import com.allinone.DevView.mypage.dto.UserProfileUpdateRequest;
import com.allinone.DevView.mypage.service.MypageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MypageController {

    private final MypageService mypageService;

    @GetMapping
    public String showMyPage(Model model) {
        Long userId = SecurityUtils.getUserId();

        MypageResponseDto userInfo = mypageService.getMypageData(userId);
        model.addAttribute("user", userInfo);

        ScoreGraphDto scoreGraph = mypageService.getScoreGraphData(userId);
        model.addAttribute("scoreGraph", scoreGraph);

        CareerChartDto careerChart = mypageService.getCareerChartData(userId);
        model.addAttribute("careerChart", careerChart);

        return "mypage/mypage";
    }


    @GetMapping("/edit")
    public String showEditForm(Model model) {
        Long userId = SecurityUtils.getUserId();

        UserProfileUpdateRequest editInfo = mypageService.getBasicUserInfo(userId);
        model.addAttribute("user", editInfo);

        return "mypage/mypage-edit";
    }

    @PostMapping("/edit")
    public String updateProfile(@ModelAttribute UserProfileUpdateRequest request) {
        Long userId = SecurityUtils.getUserId();
        mypageService.updateUserProfile(userId, request);
        return "redirect:/mypage";
    }
}
