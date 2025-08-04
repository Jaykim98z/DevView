package com.allinone.DevView.community.controller;

import com.allinone.DevView.community.dto.PostResponseDto;
import com.allinone.DevView.community.service.CommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @GetMapping("/community")
    public String communityPage(Model model) {
        List<PostResponseDto> postList = communityService.getAllPosts();

        model.addAttribute("postList", postList);
        model.addAttribute("sort", "latest");
        model.addAttribute("category", "ALL");
        model.addAttribute("level", "JUNIOR");
        model.addAttribute("query", "");

        return "community/community";
    }
}
