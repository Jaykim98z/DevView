package com.devview.mypage.controller;

import com.devview.common.util.SecurityUtils;
import com.devview.mypage.service.ProfileImageService;
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
        Long userId = SecurityUtils.getUserId(request.getUserPrincipal());
        profileImageService.uploadProfileImage(userId, imageFile);
        return "redirect:/mypage";
    }

    @PostMapping("/delete")
    public String deleteProfileImage(HttpServletRequest request) {
        Long userId = SecurityUtils.getUserId(request.getUserPrincipal());
        profileImageService.deleteProfileImage(userId);
        return "redirect:/mypage";
    }
}
