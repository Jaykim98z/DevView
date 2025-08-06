package com.allinone.DevView.mypage.controller;

import com.allinone.DevView.common.util.SecurityUtils;
import com.allinone.DevView.mypage.service.ProfileImageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage/profile/image")
public class ProfileImageController {

    private final ProfileImageService profileImageService;

    @PostMapping
    public String uploadProfileImage(@RequestParam("imageFile") MultipartFile imageFile,
                                     HttpServletRequest request) {
        Long userId = SecurityUtils.getUserId(request.getUserPrincipal());  // 객체가 아닌 메서드로 호출
        profileImageService.uploadProfileImage(userId, imageFile);
        return "redirect:/mypage";
    }

    @PostMapping("/delete")
    public String deleteProfileImage(HttpServletRequest request) {
        Long userId = SecurityUtils.getUserId(request.getUserPrincipal());  // 객체가 아닌 메서드로 호출
        profileImageService.deleteProfileImage(userId);
        return "redirect:/mypage";
    }
}
