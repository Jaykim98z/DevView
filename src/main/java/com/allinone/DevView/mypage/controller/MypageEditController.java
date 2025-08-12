package com.allinone.DevView.mypage.controller;

import com.allinone.DevView.mypage.dto.UserProfileUpdateRequest;
import com.allinone.DevView.mypage.service.MypageService;
import com.allinone.DevView.user.dto.response.UserResponse;
import jakarta.servlet.http.HttpSession;
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
public class MypageEditController {

    private final MypageService mypageService;

    /*** 마이페이지 수정 폼 뷰 */
    @GetMapping("/edit")
    public String editPage(Model model, HttpSession session) {
        UserResponse loginUser = (UserResponse) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/user/login";
        }

        model.addAttribute("user", mypageService.getBasicUserInfo(loginUser.getUserId()));
        return "mypage/mypage-edit";
    }

    /*** 사용자 정보 업데이트 처리 */
    @PostMapping("/edit")
    public String updateProfile(@ModelAttribute UserProfileUpdateRequest request, HttpSession session) {
        UserResponse loginUser = (UserResponse) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/user/login";
        }

        mypageService.updateUserProfile(loginUser.getUserId(), request);
        return "redirect:/mypage";
    }
}
