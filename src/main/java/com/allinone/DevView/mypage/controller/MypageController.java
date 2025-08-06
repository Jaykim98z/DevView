package com.allinone.DevView.mypage.controller;

import com.allinone.DevView.common.util.SecurityUtils;
import com.allinone.DevView.mypage.dto.ScoreGraphDto;
import com.allinone.DevView.mypage.dto.UserProfileUpdateRequest;
import com.allinone.DevView.mypage.service.MypageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MypageController {

    private final MypageService mypageService;

    @GetMapping
    public String showMyPage(Model model, Principal principal) {
        if (principal == null) {
            // 로그인 안 되어 있으면 로그인 페이지로 리다이렉트
            return "redirect:/login";
        }

        Long userId = SecurityUtils.getUserId(principal);
        model.addAttribute("user", mypageService.getMypageData(userId));

        ScoreGraphDto scoreGraph = mypageService.getScoreGraphData(userId);
        model.addAttribute("scoreGraph", scoreGraph);

        return "mypage/mypage";
    }


//    @GetMapping("/edit")
//    public String showEditForm(Model model, Principal principal) {
//        Long userId = SecurityUtils.getUserId(principal);
//        model.addAttribute("user", mypageService.getBasicUserInfo(userId));
//        return "mypage/mypage-edit";
//    }

//    @PostMapping("/edit")
//    public String updateProfile(@ModelAttribute UserProfileUpdateRequest request, Principal principal) {
//        Long userId = SecurityUtils.getUserId(principal);
//        mypageService.updateUserProfile(userId, request);
//        return "redirect:/mypage";
//    }
}
