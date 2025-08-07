package com.allinone.DevView.community.controller;

import com.allinone.DevView.community.dto.CommunityPostsDto;
import com.allinone.DevView.community.entity.CommunityPosts;
import com.allinone.DevView.community.service.CommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/community")
@RequiredArgsConstructor
public class CommunityViewController {

    private final CommunityService communityService;

    @GetMapping("/community")
    public String showCommunityPage(Model model) {
        List<CommunityPosts> posts = communityService.getAllPostsWithUserData();
        model.addAttribute("posts", posts);
        return "community/community";
    }
}