package com.allinone.DevView.mypage.controller;

import com.devview.mypage.dto.UserProfileUpdateRequest;
import com.devview.mypage.service.MypageService;
import com.devview.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MypageEditController {

    private final MypageService mypageService;

    @GetMapping("/edit")
    public String editPage(Model model) {
        Long userId = getCurrentUserId();
        model.addAttribute("user", mypageService.getBasicUserInfo(userId));
        return "mypage/editProfile";
    }

    @PostMapping("/edit")
    public String updateProfile(@ModelAttribute UserProfileUpdateRequest request) {
        Long userId = getCurrentUserId();
        mypageService.updateUserProfile(userId, request);
        return "redirect:/mypage";
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getId();
    }
}
