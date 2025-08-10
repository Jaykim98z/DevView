package com.allinone.DevView.mypage.controller;

import com.allinone.DevView.common.util.SecurityUtils;
import com.allinone.DevView.mypage.service.MypageService;
import com.allinone.DevView.mypage.service.ProfileImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage/profile/image")
public class ProfileImageController {

    private final ProfileImageService profileImageService;
    private final MypageService mypageService;

    @PostMapping
    public String uploadProfileImage(@RequestParam("imageFile") MultipartFile imageFile, Model model) {
        Long userId = SecurityUtils.getUserId();
        String imageUrl = profileImageService.uploadProfileImage(userId, imageFile);

        model.addAttribute("uploadedImageUrl", imageUrl);
        model.addAttribute("user", mypageService.getBasicUserInfo(userId));

        return "mypage/mypage-edit";
    }

    @PostMapping("/delete")
    public String deleteProfileImage(@RequestParam("imageUrl") String imageUrl, Model model) {
        Long userId = SecurityUtils.getUserId();
        profileImageService.deleteProfileImage(userId, imageUrl);

        model.addAttribute("uploadedImageUrl", null);
        model.addAttribute("user", mypageService.getBasicUserInfo(userId));

        return "mypage/mypage-edit";
    }
}
